# NIYVEXA — backend activation checklist

The public GitHub Pages site is a static front end. Real authentication and shared data require a Supabase project. This repository intentionally does **not** contain project credentials.

## 1. Create and configure the backend
1. Create a Supabase project in your own account.
2. In **SQL Editor**, run `supabase-schema.sql` from this folder.
3. In **Project Settings → API**, copy the Project URL and the public publishable/anon key.
4. Put those two public client values in `auth-config.js`. Never put a `service_role` or secret key in this repository.
5. In **Authentication → URL Configuration**, add the deployed app URL to Site URL / Redirect URLs:
   `https://saiftt942-max.github.io/niyvexa/app.html`
6. Enable Email OTP. To enable Google/Facebook, configure each provider's OAuth credentials in Supabase and register its callback URL with that provider. The front-end buttons alone cannot enable OAuth.
7. Test email login, OAuth callback, sign-out/session expiry, and RLS using two separate accounts.

## 2. Important limitations
- The current `app.html` still runs its discovery, likes, matches, and messages as local demo state. Creating these tables does **not** automatically connect those controls to the database. A separate integration pass and live tests are required before calling the service production-ready.
- The schema stores date of birth to enforce an adult-only profile; do not display the full birth date publicly. A restricted discovery view and server-side validation should be added before launch.
- Photo upload, moderation dashboard, verification, notifications, billing, refunds, deletion/export requests, and abuse-rate limits are not implemented.
- Do not collect real users' sensitive data until the privacy notice, data retention, moderation, and incident response process are finalized.
- Do not enable paid subscriptions until a payment provider legally available to the business in Algeria is confirmed and tested.

## 3. Launch gates
- [ ] Email login works on the published HTTPS domain.
- [ ] Google/Facebook OAuth works only after provider setup.
- [ ] RLS isolation passes tests with two unrelated accounts.
- [ ] Likes create a match only after reciprocal likes.
- [ ] Messages are visible only to the two matched users.
- [ ] Blocking prevents discovery and messaging in both directions.
- [ ] Reports reach a trusted moderator without exposing report records to other users.
- [ ] Adult-only enforcement is tested server-side.
- [ ] Privacy/terms are reviewed for the actual operating jurisdiction.
- [ ] Test on Android Chrome and a slow connection.
- [ ] Complete a release build and security review before public promotion.
