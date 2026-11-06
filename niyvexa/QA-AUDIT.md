# NIYVEXA — QA audit and repair log

Last reviewed: 2026-11-06
Scope: static code review of the public GitHub Pages prototype. No live browser automation or real-user backend test was available, so this is not a complete runtime certification.

## Confirmed blockers

1. Authentication is not configured. `auth-config.js` contains placeholder Supabase URL and key. Google, Facebook and email magic-link login cannot work until a real project and provider settings are configured.
2. No remote backend is implemented for profiles, likes, matches, messages, blocks or reports. These are local browser demo state only.
3. Six hard-coded fictional profiles are the only discovery data. No real matching community exists.
4. The match action uses a hard-coded `mutualInterest` boolean on fictional profiles; it does not represent a second real user taking an action.
5. No image upload or profile photo handling exists.
6. Payments/subscriptions are not implemented.
7. Privacy and terms pages explicitly describe themselves as prototypes and are not production-ready legal documents.
8. No live browser/device test has been run; layout, install prompt, external CDN availability and navigation still need real-device verification.

## Fixes applied in this pass

- Age validation no longer silently converts a blank or under-18 age to 18; invalid values are rejected when saving preferences.
- Saving preferences refreshes both discovery and matches.
- Resetting filters refreshes both discovery and matches.
- Demo entry asks for explicit confirmation of age 18+ and clarifies that profiles are fictional.
- Replaced the inconsistent single-letter app header mark with a heart glyph.
- Bumped the service-worker cache and registration version to v3 to reduce stale UI after deployment.

## Next priority

1. Verify deployment and test all flows on mobile Chrome.
2. Decide on a real backend/auth provider and configure email first; configure Google/Facebook OAuth only after their provider credentials and redirect URLs are available.
3. Design database tables and row-level security for profiles, preferences, likes, mutual matches, messages, blocks and reports.
4. Add photo upload with size/type checks and private storage rules.
5. Add reporting/moderation workflows, account deletion, age assurance and production privacy/terms review.
6. Implement and test payment integration only after core account and safety systems work.

## Release status

**Prototype only — not ready for public dating-service launch.** Do not describe sign-in, matching, chat, verification, payments or moderation as production features until each is connected and tested.


## Work added on 2026-11-06
- Added `supabase-schema.sql` with account profiles, likes, reciprocal-match trigger, match-scoped messages, blocks, reports, row-level security policies, and a private birth-date table plus adult-profile RPC.
- Added `SUPABASE-SETUP.md` describing external project/provider setup and launch gates.
- Added `robots.txt` to allow the landing page while excluding the demo app from indexing.
- Added a GitHub Actions workflow to validate the manifest, inline JavaScript syntax, and required site files.
- The schema has not yet been applied to a real Supabase project. The front-end still uses local demo state; database integration, OAuth provider setup, live testing, and payments remain blocked on external configuration and further development.


## Additional QA improvements — 2026-11-06
- Quick filters now reject invalid numeric ranges instead of silently coercing the maximum age upward.
- Profile saving now requires whole-number age bounds and a whole-number distance within the supported range.
- Quick-filter changes refresh both discovery and match views.
- Service-worker cache is now v4 and includes `auth-config.js` in the offline core.
- Added `.github/workflows/niyvexa-qa.yml` to check inline JavaScript syntax, manifest metadata, required files, and selected app invariants on pushes and pull requests.

These are static checks and code-level corrections only. The GitHub Actions workflow has been committed but its run result has not yet been observed. No real Supabase project, real user matching, or payment processing is active.


## Security hardening — 2026-11-06
- Enabled row-level security on `private.profile_private` and explicitly revoked schema/table privileges from `PUBLIC`, `anon`, and `authenticated` as defense in depth for birth dates.
- Extended static CI checks to fail if either private-table protection is removed.
- This does not replace applying the SQL to Supabase and testing with real authenticated accounts; the backend remains unconfigured.
