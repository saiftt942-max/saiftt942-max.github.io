-- NIYVEXA backend foundation — apply in Supabase SQL Editor.
-- Requires Supabase Auth. Review policies before production launch.
create extension if not exists pgcrypto;
create schema if not exists private;

create table if not exists public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  display_name text not null default '' check (char_length(display_name) <= 60),
  gender text check (gender in ('man','woman','nonbinary','other')),
  city text not null default '' check (char_length(city) <= 100),
  bio text not null default '' check (char_length(bio) <= 500),
  intents text[] not null default '{}'::text[],
  looking_for_genders text[] not null default array['man','woman']::text[],
  min_age smallint not null default 18 check (min_age between 18 and 99),
  max_age smallint not null default 99 check (max_age between min_age and 99),
  max_distance_km integer not null default 100 check (max_distance_km between 1 and 5000),
  is_visible boolean not null default false,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
);

-- Keep full birth dates outside the API-exposed public schema.
create table if not exists private.profile_private (
  user_id uuid primary key references public.profiles(id) on delete cascade,
  birth_date date not null,
  updated_at timestamptz not null default now()
);

create or replace function public.is_adult_profile(profile_id uuid)
returns boolean language sql stable security definer set search_path = ''
as $
  select exists (
    select 1 from private.profile_private pp
    where pp.user_id = profile_id
      and pp.birth_date <= (current_date - interval '18 years')::date
  );
$;
revoke all on function public.is_adult_profile(uuid) from public;
grant execute on function public.is_adult_profile(uuid) to authenticated;

create or replace function private.validate_birth_date()
returns trigger language plpgsql set search_path = ''
as $
begin
  if new.birth_date > (current_date - interval '18 years')::date then
    raise exception 'NIYVEXA is for adults aged 18 and over';
  end if;
  new.updated_at = now();
  return new;
end; $;
drop trigger if exists profile_private_validate_birth_date on private.profile_private;
create trigger profile_private_validate_birth_date before insert or update on private.profile_private
for each row execute function private.validate_birth_date();

create table if not exists public.likes (
  id uuid primary key default gen_random_uuid(),
  from_user uuid not null references public.profiles(id) on delete cascade,
  to_user uuid not null references public.profiles(id) on delete cascade,
  created_at timestamptz not null default now(),
  constraint likes_no_self check (from_user <> to_user),
  constraint likes_unique_pair unique (from_user, to_user)
);
create index if not exists likes_to_user_idx on public.likes(to_user);

create table if not exists public.matches (
  id uuid primary key default gen_random_uuid(),
  user_a uuid not null references public.profiles(id) on delete cascade,
  user_b uuid not null references public.profiles(id) on delete cascade,
  created_at timestamptz not null default now(),
  constraint matches_no_self check (user_a <> user_b),
  constraint matches_canonical_order check (user_a < user_b),
  constraint matches_unique_pair unique (user_a, user_b)
);

create table if not exists public.blocks (
  blocker_id uuid not null references public.profiles(id) on delete cascade,
  blocked_id uuid not null references public.profiles(id) on delete cascade,
  created_at timestamptz not null default now(),
  primary key (blocker_id, blocked_id),
  constraint blocks_no_self check (blocker_id <> blocked_id)
);

create table if not exists public.messages (
  id uuid primary key default gen_random_uuid(),
  match_id uuid not null references public.matches(id) on delete cascade,
  sender_id uuid not null references public.profiles(id) on delete cascade,
  body text not null check (char_length(body) between 1 and 2000),
  created_at timestamptz not null default now()
);
create index if not exists messages_match_created_idx on public.messages(match_id, created_at);

create table if not exists public.reports (
  id uuid primary key default gen_random_uuid(),
  reporter_id uuid not null references public.profiles(id) on delete cascade,
  target_id uuid not null references public.profiles(id) on delete cascade,
  reason text not null check (reason in ('fake_profile','harassment','spam','underage','unsafe','other')),
  details text not null default '' check (char_length(details) <= 2000),
  status text not null default 'pending' check (status in ('pending','reviewing','resolved','dismissed')),
  created_at timestamptz not null default now(),
  constraint reports_no_self check (reporter_id <> target_id)
);

create or replace function public.set_updated_at()
returns trigger language plpgsql as $$
begin new.updated_at = now(); return new; end; $$;
drop trigger if exists profiles_set_updated_at on public.profiles;
create trigger profiles_set_updated_at before update on public.profiles
for each row execute function public.set_updated_at();

-- Create an empty private profile for each newly authenticated account.
create or replace function public.handle_new_user()
returns trigger language plpgsql security definer set search_path = ''
as $$
begin
  insert into public.profiles (id, display_name)
  values (new.id, coalesce(new.raw_user_meta_data ->> 'name', ''))
  on conflict (id) do nothing;
  return new;
end; $$;
drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created after insert on auth.users
for each row execute function public.handle_new_user();

-- A match is created only after the other account has also liked back.
create or replace function public.create_reciprocal_match()
returns trigger language plpgsql security definer set search_path = ''
as $$
declare a uuid; b uuid;
begin
  if exists (
    select 1 from public.likes l
    where l.from_user = new.to_user and l.to_user = new.from_user
  ) and not exists (
    select 1 from public.blocks b
    where (b.blocker_id = new.from_user and b.blocked_id = new.to_user)
       or (b.blocker_id = new.to_user and b.blocked_id = new.from_user)
  ) then
    a := least(new.from_user, new.to_user);
    b := greatest(new.from_user, new.to_user);
    insert into public.matches(user_a, user_b) values (a, b)
    on conflict (user_a, user_b) do nothing;
  end if;
  return new;
end; $$;
drop trigger if exists likes_create_reciprocal_match on public.likes;
create trigger likes_create_reciprocal_match after insert on public.likes
for each row execute function public.create_reciprocal_match();

alter table public.profiles enable row level security;
alter table public.likes enable row level security;
alter table public.matches enable row level security;
alter table public.blocks enable row level security;
alter table public.messages enable row level security;
alter table public.reports enable row level security;

drop policy if exists "profiles_select_visible_or_own" on public.profiles;
create policy "profiles_select_visible_or_own" on public.profiles for select to authenticated
using (
  id = (select auth.uid())
  or (
    is_visible = true
    and public.is_adult_profile(profiles.id)
    and not exists (
      select 1 from public.blocks b
      where (b.blocker_id = (select auth.uid()) and b.blocked_id = profiles.id)
         or (b.blocker_id = profiles.id and b.blocked_id = (select auth.uid()))
    )
  )
);
drop policy if exists "profiles_insert_own" on public.profiles;
create policy "profiles_insert_own" on public.profiles for insert to authenticated
with check (id = (select auth.uid()));
drop policy if exists "profiles_update_own" on public.profiles;
create policy "profiles_update_own" on public.profiles for update to authenticated
using (id = (select auth.uid())) with check (id = (select auth.uid()));
drop policy if exists "likes_select_participant" on public.likes;
create policy "likes_select_participant" on public.likes for select to authenticated
using (from_user = (select auth.uid()) or to_user = (select auth.uid()));
drop policy if exists "likes_insert_self" on public.likes;
create policy "likes_insert_self" on public.likes for insert to authenticated
with check (from_user = (select auth.uid()) and to_user <> (select auth.uid()));
drop policy if exists "likes_delete_self" on public.likes;
create policy "likes_delete_self" on public.likes for delete to authenticated
using (from_user = (select auth.uid()));

drop policy if exists "matches_select_member" on public.matches;
create policy "matches_select_member" on public.matches for select to authenticated
using (user_a = (select auth.uid()) or user_b = (select auth.uid()));

drop policy if exists "blocks_select_own" on public.blocks;
create policy "blocks_select_own" on public.blocks for select to authenticated
using (blocker_id = (select auth.uid()));
drop policy if exists "blocks_insert_own" on public.blocks;
create policy "blocks_insert_own" on public.blocks for insert to authenticated
with check (blocker_id = (select auth.uid()) and blocked_id <> (select auth.uid()));
drop policy if exists "blocks_delete_own" on public.blocks;
create policy "blocks_delete_own" on public.blocks for delete to authenticated
using (blocker_id = (select auth.uid()));

drop policy if exists "messages_select_match_member" on public.messages;
create policy "messages_select_match_member" on public.messages for select to authenticated
using (exists (
  select 1 from public.matches m
  where m.id = messages.match_id
    and ((m.user_a = (select auth.uid())) or (m.user_b = (select auth.uid())))
));
drop policy if exists "messages_insert_match_member" on public.messages;
create policy "messages_insert_match_member" on public.messages for insert to authenticated
with check (
  sender_id = (select auth.uid())
  and exists (
    select 1 from public.matches m
    where m.id = messages.match_id
      and ((m.user_a = (select auth.uid())) or (m.user_b = (select auth.uid())))
  )
  and not exists (
    select 1 from public.blocks b
    join public.matches m on m.id = messages.match_id
    where (b.blocker_id = m.user_a and b.blocked_id = m.user_b)
       or (b.blocker_id = m.user_b and b.blocked_id = m.user_a)
  )
);

drop policy if exists "reports_insert_self" on public.reports;
create policy "reports_insert_self" on public.reports for insert to authenticated
with check (reporter_id = (select auth.uid()) and target_id <> (select auth.uid()));
-- Reports are intentionally not readable by ordinary users. Moderation access must be
-- implemented separately with a trusted server/admin role; never expose service_role in the app.

-- Do not expose private profile fields in public APIs. Consider a restricted view for discovery
-- and add Storage policies only when real photo uploads are implemented.
