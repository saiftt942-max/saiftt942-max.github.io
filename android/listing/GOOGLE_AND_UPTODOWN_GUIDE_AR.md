# دليل النشر والفهرسة — Niyvexa

## 1) ما أصبح جاهزًا
- صفحة تعريف عامة: https://saiftt942-max.github.io/niyvexa/
- صفحة تجربة الويب: https://saiftt942-max.github.io/niyvexa/app.html
- صفحة الخصوصية الأولية: https://saiftt942-max.github.io/niyvexa/privacy.html
- شروط أولية: https://saiftt942-max.github.io/niyvexa/terms.html
- خريطة الموقع: https://saiftt942-max.github.io/sitemap.xml
- رمز التطبيق والبيانات والوصف: ضمن مجلد `android/listing` وحزمة الصور المحلية.

## 2) فهرسة Google
1. سجّل الدخول بحسابك في Google Search Console: https://search.google.com/search-console
2. أضف نطاق URL Prefix: `https://saiftt942-max.github.io/`.
3. أكمل إثبات الملكية بالطريقة التي يعرضها Search Console. لا تُشارك رمز إثبات الملكية أو بيانات حسابك.
4. افتح Sitemaps وأرسل `sitemap.xml`.
5. افحص صفحة `https://saiftt942-max.github.io/niyvexa/` باستخدام URL Inspection، ثم اطلب الفهرسة.
6. ظهور الصفحة وترتيبها ومدة الفهرسة غير مضمونة؛ إرسال الخريطة لا يعني أن Google سيعرضها فورًا.

## 3) نشر Uptodown
1. افتح [Uptodown Developers Console](https://www.uptodown.dev/sign-up) وأنشئ حساب المطوّر بنفسك.
2. قبل إرسال إصدار عام، أضف أسرار التوقيع الأربعة في GitHub Settings → Secrets and variables → Actions باستخدام ملف الإعداد الخاص. احتفظ بمفتاح التوقيع ونسخة احتياطية منه بأمان.
3. شغّل workflow `Build Niyvexa Android APK` يدويًا وانتظر حتى ينجح. حمّل الأثر `Niyvexa-0.1.0-demo-RELEASE-SIGNED`. لا ترسل ملف DEBUG.
4. في Uptodown اختر Android، أرفق APK الموقّع، واستخدم الاسم والحزمة والوصف متعدد اللغات في `uptodown-metadata.md` و`descriptions.md`. نموذج التوزيع الحالي: Demo، الإعلانات وIAP والاشتراك: No.
5. ارفع الأيقونة والبانر من حزمة الصور. التقط صور شاشة فعلية بعد تثبيت النسخة الموقعة على هاتفك. لا تستخدم صورًا مصطنعة بوصفها لقطات شاشة حقيقية.
6. قدّم التطبيق للمراجعة. التسجيل والنشر مجانيان حسب دليل Uptodown، لكن القبول غير مضمون ويخضع لمراجعة الجودة.

## 4) الميزة والقيود الحالية
Niyvexa الآن **نموذج Android أصلي يعمل محليًا**. الملفات خيالية؛ الإعجابات والمطابقات والرسائل والحظر والبلاغات لا تغادر الجهاز. لا توجد حسابات مستخدمين حقيقية، ولا محادثات عن بُعد، ولا خدمة خادم، ولا دفع أو إعلانات. لا تسوّقه باعتباره خدمة تعارف مباشرة حتى يتم بناء واختبار الخادم والتحقق من العمر والإشراف وحذف الحسابات وسياسة الخصوصية الفعلية.
