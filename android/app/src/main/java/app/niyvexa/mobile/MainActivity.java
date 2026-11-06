package app.niyvexa.mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.view.Gravity;
import android.widget.*;
import org.json.JSONArray;
import java.util.*;

public class MainActivity extends Activity {
    private static final int BG=Color.rgb(16,18,24), PANEL=Color.rgb(24,27,36), PANEL2=Color.rgb(32,36,48);
    private static final int TEXT=Color.rgb(225,227,236), MUTED=Color.rgb(165,171,192), VIOLET=Color.rgb(179,169,237);
    private SharedPreferences prefs;
    private String lang="ar", tab="Discover", chatId="";
    private LinearLayout root, body, nav;
    private Set<String> likes=new LinkedHashSet<>(), blocked=new LinkedHashSet<>(), reported=new LinkedHashSet<>();
    private final Set<String> myIntents=new LinkedHashSet<>(Arrays.asList("Friendship","Serious relationship"));
    private String name="Guest", gender="Man", city="Guelma", looking="Everyone";
    private int age=19, minAge=18, maxAge=35, maxDistance=100;
    private String filterGender="Everyone", filterIntent="Any intent", filterCity="Any city";
    private final Map<String,Map<String,String>> tr=new HashMap<>();
    private final List<Profile> profiles=new ArrayList<>();

    private static class Profile {
        String id,name,gender,city,bio; int age,distance,minAge,maxAge,maxDistance;
        String[] intents, looking;
        boolean mutual;
        Profile(String id,String name,int age,String gender,String city,int distance,String bio,String[] intents,boolean mutual,int minAge,int maxAge,int maxDistance,String[] looking){
            this.id=id;this.name=name;this.age=age;this.gender=gender;this.city=city;this.distance=distance;this.bio=bio;this.intents=intents;this.mutual=mutual;this.minAge=minAge;this.maxAge=maxAge;this.maxDistance=maxDistance;this.looking=looking;
        }
    }
    private interface Pick { void onPick(String value); }
    private interface Action { void run(); }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG); getWindow().setNavigationBarColor(BG);
        prefs=getSharedPreferences("niyvexa-demo-v1",MODE_PRIVATE);
        loadState(); initTranslations(); seedProfiles();
        buildRoot();
    }

    private void loadState(){
        lang=prefs.getString("lang","ar"); name=prefs.getString("name","Guest"); age=prefs.getInt("age",19);
        gender=prefs.getString("gender","Man"); city=prefs.getString("city","Guelma"); looking=prefs.getString("looking","Everyone");
        minAge=prefs.getInt("minAge",18);maxAge=prefs.getInt("maxAge",35);maxDistance=prefs.getInt("maxDistance",100);
        filterGender=prefs.getString("filterGender","Everyone");filterIntent=prefs.getString("filterIntent","Any intent");filterCity=prefs.getString("filterCity","Any city");
        myIntents.clear();myIntents.addAll(prefs.getStringSet("intents",new LinkedHashSet<>(Arrays.asList("Friendship","Serious relationship"))));
        likes.addAll(prefs.getStringSet("likes",Collections.emptySet()));blocked.addAll(prefs.getStringSet("blocked",Collections.emptySet()));reported.addAll(prefs.getStringSet("reported",Collections.emptySet()));
        if(!Arrays.asList("ar","en","fr","es").contains(lang))lang="ar";
    }
    private void saveState(){
        prefs.edit().putString("lang",lang).putString("name",name).putInt("age",age).putString("gender",gender).putString("city",city)
                .putString("looking",looking).putInt("minAge",minAge).putInt("maxAge",maxAge).putInt("maxDistance",maxDistance)
                .putString("filterGender",filterGender).putString("filterIntent",filterIntent).putString("filterCity",filterCity)
                .putStringSet("intents",new HashSet<>(myIntents)).putStringSet("likes",new HashSet<>(likes))
                .putStringSet("blocked",new HashSet<>(blocked)).putStringSet("reported",new HashSet<>(reported)).apply();
    }
    private void seedProfiles(){
        profiles.clear();
        profiles.add(new Profile("p1","Yacine",24,"Man","Guelma",3,"Tech, evening walks and thoughtful conversations.",new String[]{"Friendship","Serious relationship"},true,18,36,120,new String[]{"Man","Woman"}));
        profiles.add(new Profile("p2","Maya",22,"Woman","Guelma",5,"Music, coffee, languages and quiet weekends.",new String[]{"Social connection","Friendship"},false,18,32,80,new String[]{"Man","Woman"}));
        profiles.add(new Profile("p3","Nour",25,"Woman","Constantine",58,"Books, design and direct communication.",new String[]{"Serious relationship","Friendship"},true,20,35,100,new String[]{"Man","Woman"}));
        profiles.add(new Profile("p4","Samir",21,"Man","Setif",71,"Learning new skills and meeting people respectfully.",new String[]{"Exploration","Social connection"},false,18,30,150,new String[]{"Man","Woman"}));
        profiles.add(new Profile("p5","Lina",27,"Woman","Algiers",190,"Honesty, clear intentions and mutual respect.",new String[]{"Serious relationship"},true,23,38,250,new String[]{"Man","Woman"}));
        profiles.add(new Profile("p6","Amine",29,"Man","Constantine",63,"Hiking, film and good conversation.",new String[]{"Friendship","Social connection"},false,21,35,130,new String[]{"Man","Woman"}));
    }

    private void initTranslations(){
        addPack("ar", new String[][]{
            {"Intent first","النية أولًا"},{"DEMO • 18+","معاينة • 18+"},{"Fictional profiles only. Everything stays on this device; no real user receives likes or messages.","ملفات خيالية فقط. كل شيء يبقى على هذا الجهاز؛ لا يتلقى أي مستخدم حقيقي إعجابات أو رسائل."},
            {"Discover","استكشاف"},{"Preferences","تفضيلاتي"},{"Matches","المطابقات"},{"Messages","الرسائل"},{"Plus","الربح"},{"Safety","الأمان"},
            {"Mutual-intent discovery","اكتشاف قائم على النية المتبادلة"},{"Pick your intentions and boundaries before browsing. This is an offline demo, not a live dating service.","حدّد نياتك وحدودك قبل الاستكشاف. هذه معاينة تعمل دون اتصال وليست خدمة تعارف مباشرة."},
            {"Filter profiles","تصفية الملفات"},{"Who do you want to meet?","من تريد أن تتعرف إليه؟"},{"Everyone","الجميع"},{"Men","رجال"},{"Women","نساء"},
            {"Minimum age","أقل عمر"},{"Maximum age","أعلى عمر"},{"Max distance (km)","أقصى مسافة (كم)"},{"Intent","النية"},{"Any intent","أي نية"},{"Friendship","صداقة"},{"Social connection","تعارف اجتماعي"},{"Serious relationship","علاقة جادة"},{"Exploration","استكشاف"},
            {"City","المدينة"},{"Any city","أي مدينة"},{"Guelma","قالمة"},{"Constantine","قسنطينة"},{"Algiers","الجزائر العاصمة"},{"Setif","سطيف"},
            {"Apply filters","تطبيق الفلاتر"},{"Reset filters","إعادة ضبط الفلاتر"},{"Nothing fits yet. Try a wider age range, another intent or a longer distance.","لا توجد نتائج مناسبة الآن. جرّب توسيع العمر أو اختيار نية أخرى أو زيادة المسافة."},
            {"Fictional demo profile","ملف خيالي تجريبي"},{"years","سنة"},{"away","على بُعد"},{"Mutual demo interest","اهتمام تجريبي متبادل"},{"Local demo match created; no person was notified.","أُنشئت مطابقة تجريبية محلية؛ لم يُخطر أي شخص."},
            {"Show interest","إبداء الاهتمام"},{"Interested","تم إبداء الاهتمام"},{"Block","حظر"},{"Report","إبلاغ"},{"Block this fictional profile on this device?","هل تريد حظر هذا الملف الخيالي على هذا الجهاز؟"},{"Profile blocked locally.","حُظر الملف محليًا."},{"Demo report saved only on this device; nothing was sent.","حُفظ البلاغ على هذا الجهاز فقط؛ لم يُرسل شيء."},
            {"Your profile & boundaries","ملفك وحدودك"},{"Your display name","اسم العرض"},{"Your age (18+)","عمرك (18+)"},
            {"Your gender","جنسك"},{"Man","رجل"},{"Woman","امرأة"},{"Your city","مدينتك"},{"Maximum distance you accept (km)","أقصى مسافة تقبلها (كم)"},{"Who you want to meet","من تريد التعرف إليه"},{"Your age range","الفئة العمرية التي تقبلها"},{"People aged at least","الأشخاص من عمر"},{"People aged at most","الأشخاص حتى عمر"},{"Choose your intentions","اختر نياتك"},{"Save preferences","حفظ التفضيلات"},{"Preferences saved on this device.","حُفظت التفضيلات على هذا الجهاز."},{"Please enter an age from 18 to 99 and select at least one intention.","أدخل عمرًا بين 18 و99 واختر نية واحدة على الأقل."},
            {"No local matches yet.","لا توجد مطابقات محلية بعد."},{"Your matches exist only in this demo. A match is simulated when a profile is marked as mutually interested.","مطابقاتك موجودة داخل المعاينة فقط. تُحاكى المطابقة إذا كان الملف مهيأً للاهتمام المتبادل."},{"Open demo chat","فتح محادثة تجريبية"},{"No matches are available for chatting yet.","لا توجد مطابقات يمكن مراسلتها بعد."},{"Messages stay on this device. No other person can read or reply to them.","تبقى الرسائل على هذا الجهاز. لا يستطيع شخص آخر قراءتها أو الرد عليها."},{"Write a message…","اكتب رسالة…"},{"Send","إرسال"},{"Message saved locally.","حُفظت الرسالة محليًا."},
            {"Business model","نموذج الربح"},{"Free core","الأساس المجاني"},{"Discovery, mutual preference filters and fictional test profiles.","الاستكشاف والفلاتر المتبادلة والملفات الخيالية للاختبار."},{"Planned — not active","مخطط له — غير مفعّل"},{"BEYOND Plus","الاشتراك المميز"},{"Optional advanced filters could be paid once real accounts, a secure server and a lawful payment provider are ready.","يمكن جعل الفلاتر المتقدمة مدفوعة اختياريًا بعد إعداد الحسابات والخادم الآمن ومزود دفع قانوني."},{"Limited ads — not active","إعلانات محدودة — غير مفعّلة"},{"No ad network or payment system is connected. This version earns no money.","لا توجد شبكة إعلانات أو منظومة دفع. هذه النسخة لا تحقق أي دخل."},
            {"Safety & local data","الأمان والبيانات المحلية"},{"Blocked profiles","الملفات المحظورة"},{"Saved local reports","البلاغات المحفوظة محليًا"},{"Delete all demo data","حذف جميع بيانات المعاينة"},{"This clears your saved preferences, likes, blocks, reports and local messages from this app.","سيحذف ذلك تفضيلاتك وإعجاباتك وحظرَك وبلاغاتك ورسائلك المحلية من التطبيق."},{"Delete local data?","هل تريد حذف البيانات المحلية؟"},{"All local demo data was deleted.","حُذفت جميع بيانات المعاينة المحلية."},{"Cancel","إلغاء"},{"Delete","حذف"},{"Preview only — not a public service yet","معاينة فقط — ليست خدمة عامة بعد"},{"Language","اللغة"},{"No data leaves your phone.","لا تغادر البيانات هاتفك."}
        });
        addPack("fr", new String[][]{
            {"Intent first","L’intention d’abord"},{"DEMO • 18+","DÉMO • 18+"},{"Fictional profiles only. Everything stays on this device; no real user receives likes or messages.","Profils fictifs uniquement. Tout reste sur cet appareil ; aucun utilisateur réel ne reçoit de mentions J’aime ni de messages."},
            {"Discover","Découvrir"},{"Preferences","Préférences"},{"Matches","Compatibilités"},{"Messages","Messages"},{"Plus","Revenus"},{"Safety","Sécurité"},
            {"Mutual-intent discovery","Rencontres fondées sur l’intention mutuelle"},{"Pick your intentions and boundaries before browsing. This is an offline demo, not a live dating service.","Choisissez vos intentions et limites avant de parcourir les profils. Il s’agit d’une démo hors ligne, pas d’un service actif."},
            {"Filter profiles","Filtrer les profils"},{"Who do you want to meet?","Qui souhaitez-vous rencontrer ?"},{"Everyone","Tout le monde"},{"Men","Hommes"},{"Women","Femmes"},
            {"Minimum age","Âge minimum"},{"Maximum age","Âge maximum"},{"Max distance (km)","Distance max. (km)"},{"Intent","Intention"},{"Any intent","Toute intention"},{"Friendship","Amitié"},{"Social connection","Lien social"},{"Serious relationship","Relation sérieuse"},{"Exploration","Exploration"},
            {"City","Ville"},{"Any city","Toutes les villes"},{"Guelma","Guelma"},{"Constantine","Constantine"},{"Algiers","Alger"},{"Setif","Sétif"},
            {"Apply filters","Appliquer les filtres"},{"Reset filters","Réinitialiser les filtres"},{"Nothing fits yet. Try a wider age range, another intent or a longer distance.","Aucun profil ne correspond. Élargissez l’âge, choisissez une autre intention ou augmentez la distance."},
            {"Fictional demo profile","Profil fictif de démonstration"},{"years","ans"},{"away","de distance"},{"Mutual demo interest","Intérêt réciproque fictif"},{"Local demo match created; no person was notified.","Compatibilité locale créée ; personne n’a été averti."},
            {"Show interest","Montrer son intérêt"},{"Interested","Intéressé"},{"Block","Bloquer"},{"Report","Signaler"},{"Block this fictional profile on this device?","Bloquer ce profil fictif sur cet appareil ?"},{"Profile blocked locally.","Profil bloqué localement."},{"Demo report saved only on this device; nothing was sent.","Signalement enregistré uniquement sur cet appareil ; rien n’a été envoyé."},
            {"Your profile & boundaries","Votre profil et vos limites"},{"Your display name","Nom affiché"},{"Your age (18+)","Votre âge (18+)"},
            {"Your gender","Votre genre"},{"Man","Homme"},{"Woman","Femme"},{"Your city","Votre ville"},{"Maximum distance you accept (km)","Distance maximale acceptée (km)"},{"Who you want to meet","Qui souhaitez-vous rencontrer"},{"Your age range","Tranche d’âge recherchée"},{"People aged at least","Personnes âgées d’au moins"},{"People aged at most","Personnes âgées d’au plus"},{"Choose your intentions","Choisissez vos intentions"},{"Save preferences","Enregistrer les préférences"},{"Preferences saved on this device.","Préférences enregistrées sur cet appareil."},{"Please enter an age from 18 to 99 and select at least one intention.","Indiquez un âge de 18 à 99 ans et choisissez au moins une intention."},
            {"No local matches yet.","Aucune compatibilité locale pour le moment."},{"Your matches exist only in this demo. A match is simulated when a profile is marked as mutually interested.","Les compatibilités n’existent que dans cette démo. Une compatibilité est simulée si un profil est configuré comme intéressé en retour."},{"Open demo chat","Ouvrir le chat démo"},{"No matches are available for chatting yet.","Aucune compatibilité à contacter pour l’instant."},{"Messages stay on this device. No other person can read or reply to them.","Les messages restent sur cet appareil. Aucune autre personne ne peut les lire ni y répondre."},{"Write a message…","Écrire un message…"},{"Send","Envoyer"},{"Message saved locally.","Message enregistré localement."},
            {"Business model","Modèle économique"},{"Free core","Fonctions essentielles gratuites"},{"Discovery, mutual preference filters and fictional test profiles.","Découverte, filtres réciproques et profils fictifs de test."},{"Planned — not active","Prévu — inactif"},{"BEYOND Plus","Abonnement premium"},{"Optional advanced filters could be paid once real accounts, a secure server and a lawful payment provider are ready.","Des filtres avancés pourraient être payants une fois les comptes réels, le serveur sécurisé et un prestataire de paiement adapté en place."},{"Limited ads — not active","Publicités limitées — inactives"},{"No ad network or payment system is connected. This version earns no money.","Aucun réseau publicitaire ni paiement n’est connecté. Cette version ne génère aucun revenu."},
            {"Safety & local data","Sécurité et données locales"},{"Blocked profiles","Profils bloqués"},{"Saved local reports","Signalements locaux"},{"Delete all demo data","Supprimer toutes les données démo"},{"This clears your saved preferences, likes, blocks, reports and local messages from this app.","Cela supprime les préférences, mentions J’aime, blocages, signalements et messages locaux enregistrés dans l’application."},{"Delete local data?","Supprimer les données locales ?"},{"All local demo data was deleted.","Toutes les données locales de démo ont été supprimées."},{"Cancel","Annuler"},{"Delete","Supprimer"},{"Preview only — not a public service yet","Aperçu uniquement — pas encore un service public"},{"Language","Langue"},{"No data leaves your phone.","Aucune donnée ne quitte votre téléphone."}
        });
        addPack("es", new String[][]{
            {"Intent first","La intención primero"},{"DEMO • 18+","DEMOSTRACIÓN • 18+"},{"Fictional profiles only. Everything stays on this device; no real user receives likes or messages.","Solo perfiles ficticios. Todo permanece en este dispositivo; ningún usuario real recibe «me gusta» ni mensajes."},
            {"Discover","Explorar"},{"Preferences","Preferencias"},{"Matches","Coincidencias"},{"Messages","Mensajes"},{"Plus","Ingresos"},{"Safety","Seguridad"},
            {"Mutual-intent discovery","Conexiones basadas en la intención mutua"},{"Pick your intentions and boundaries before browsing. This is an offline demo, not a live dating service.","Elige tus intenciones y límites antes de explorar. Es una demostración sin conexión, no un servicio de citas activo."},
            {"Filter profiles","Filtrar perfiles"},{"Who do you want to meet?","¿A quién quieres conocer?"},{"Everyone","Todas las personas"},{"Men","Hombres"},{"Women","Mujeres"},
            {"Minimum age","Edad mínima"},{"Maximum age","Edad máxima"},{"Max distance (km)","Distancia máxima (km)"},{"Intent","Intención"},{"Any intent","Cualquier intención"},{"Friendship","Amistad"},{"Social connection","Conexión social"},{"Serious relationship","Relación seria"},{"Exploration","Exploración"},
            {"City","Ciudad"},{"Any city","Cualquier ciudad"},{"Guelma","Guelma"},{"Constantine","Constantina"},{"Algiers","Argel"},{"Setif","Sétif"},
            {"Apply filters","Aplicar filtros"},{"Reset filters","Restablecer filtros"},{"Nothing fits yet. Try a wider age range, another intent or a longer distance.","No hay perfiles compatibles. Amplía la edad, elige otra intención o aumenta la distancia."},
            {"Fictional demo profile","Perfil ficticio de prueba"},{"years","años"},{"away","de distancia"},{"Mutual demo interest","Interés mutuo de prueba"},{"Local demo match created; no person was notified.","Se creó una coincidencia local de prueba; no se notificó a nadie."},
            {"Show interest","Mostrar interés"},{"Interested","Interesado"},{"Block","Bloquear"},{"Report","Denunciar"},{"Block this fictional profile on this device?","¿Bloquear este perfil ficticio en este dispositivo?"},{"Profile blocked locally.","Perfil bloqueado localmente."},{"Demo report saved only on this device; nothing was sent.","El reporte se guardó solo en este dispositivo; no se envió nada."},
            {"Your profile & boundaries","Tu perfil y tus límites"},{"Your display name","Nombre visible"},{"Your age (18+)","Tu edad (18+)"},
            {"Your gender","Tu género"},{"Man","Hombre"},{"Woman","Mujer"},{"Your city","Tu ciudad"},{"Maximum distance you accept (km)","Distancia máxima aceptada (km)"},{"Who you want to meet","A quién quieres conocer"},{"Your age range","Rango de edad buscado"},{"People aged at least","Personas de al menos"},{"People aged at most","Personas de hasta"},{"Choose your intentions","Elige tus intenciones"},{"Save preferences","Guardar preferencias"},{"Preferences saved on this device.","Preferencias guardadas en este dispositivo."},{"Please enter an age from 18 to 99 and select at least one intention.","Introduce una edad de 18 a 99 años y elige al menos una intención."},
            {"No local matches yet.","Todavía no hay coincidencias locales."},{"Your matches exist only in this demo. A match is simulated when a profile is marked as mutually interested.","Las coincidencias solo existen en esta demostración. Se simula una coincidencia si el perfil está configurado con interés mutuo."},{"Open demo chat","Abrir chat de prueba"},{"No matches are available for chatting yet.","Todavía no hay coincidencias para chatear."},{"Messages stay on this device. No other person can read or reply to them.","Los mensajes permanecen en este dispositivo. Ninguna otra persona puede leerlos ni responderlos."},{"Write a message…","Escribe un mensaje…"},{"Send","Enviar"},{"Message saved locally.","Mensaje guardado localmente."},
            {"Business model","Modelo de negocio"},{"Free core","Funciones básicas gratis"},{"Discovery, mutual preference filters and fictional test profiles.","Exploración, filtros mutuos y perfiles ficticios de prueba."},{"Planned — not active","Previsto — no activo"},{"BEYOND Plus","Suscripción premium"},{"Optional advanced filters could be paid once real accounts, a secure server and a lawful payment provider are ready.","Los filtros avanzados podrían ser de pago cuando haya cuentas reales, un servidor seguro y un proveedor de pagos adecuado."},{"Limited ads — not active","Anuncios limitados — inactivos"},{"No ad network or payment system is connected. This version earns no money.","No hay red publicitaria ni sistema de pago conectado. Esta versión no genera ingresos."},
            {"Safety & local data","Seguridad y datos locales"},{"Blocked profiles","Perfiles bloqueados"},{"Saved local reports","Reportes locales guardados"},{"Delete all demo data","Eliminar todos los datos de prueba"},{"This clears your saved preferences, likes, blocks, reports and local messages from this app.","Esto elimina las preferencias, los «me gusta», los bloqueos, los reportes y los mensajes locales guardados en esta aplicación."},{"Delete local data?","¿Eliminar datos locales?"},{"All local demo data was deleted.","Se eliminaron todos los datos locales de prueba."},{"Cancel","Cancelar"},{"Delete","Eliminar"},{"Preview only — not a public service yet","Solo vista previa: aún no es un servicio público"},{"Language","Idioma"},{"No data leaves your phone.","Ningún dato sale de tu teléfono."}
        });
    }

    private void addPack(String language, String[][] pairs) {
        Map<String,String> map=new HashMap<>();
        for(String[] p:pairs) if(p.length>=2) map.put(p[0],p[1]);
        tr.put(language,map);
    }
    private String t(String key) { Map<String,String> m=tr.get(lang); return m!=null&&m.containsKey(key)?m.get(key):key; }
    private int dp(float value){return (int)(value*getResources().getDisplayMetrics().density+0.5f);}
    private GradientDrawable shape(int color,int radius,int strokeColor){
        GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));if(strokeColor!=0)d.setStroke(dp(1),strokeColor);return d;
    }
    private TextView text(String value,int size,boolean bold,int color){
        TextView v=new TextView(this);v.setText(value);v.setTextSize(size);v.setTextColor(color);if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        v.setGravity(lang.equals("ar")?Gravity.RIGHT:Gravity.LEFT);v.setIncludeFontPadding(true);return v;
    }
    private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    private LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h));}
    private void gap(LinearLayout into,int h){View v=new View(this);into.addView(v,lp(1,h));}
    private void pad(View v,int x,int y){v.setPadding(dp(x),dp(y),dp(x),dp(y));}
    private void addText(LinearLayout into,String value,int size,boolean bold,int color,int top){
        TextView v=text(value,size,bold,color);LinearLayout.LayoutParams p=lp(-1,-2);p.topMargin=dp(top);into.addView(v,p);
    }
    private LinearLayout card(String heading,String sub){
        LinearLayout c=column();c.setBackground(shape(PANEL,16,0xff303546));pad(c,15,14);
        if(heading!=null)addText(c,heading,16,true,TEXT,0);
        if(sub!=null&&!sub.isEmpty())addText(c,sub,12,false,MUTED,5);
        LinearLayout.LayoutParams p=lp(-1,-2);p.bottomMargin=dp(12);body.addView(c,p);return c;
    }
    private Button button(String label,Action action,boolean primary){
        Button b=new Button(this);b.setText(label);b.setTextSize(12);b.setAllCaps(false);b.setTextColor(primary?0xff171522:TEXT);b.setPadding(dp(10),dp(3),dp(10),dp(3));
        b.setBackground(shape(primary?VIOLET:PANEL2,12,primary?VIOLET:0xff41475a));b.setMinHeight(dp(43));b.setOnClickListener(v->action.run());return b;
    }
    private void addButton(LinearLayout into,String label,Action action,boolean primary){
        LinearLayout.LayoutParams p=lp(-1,-2);p.topMargin=dp(8);into.addView(button(label,action,primary),p);
    }
    private void buildRoot(){
        root=column();root.setBackgroundColor(BG);
        LinearLayout head=row();head.setPadding(dp(16),dp(11),dp(16),dp(10));head.setBackgroundColor(BG);
        LinearLayout logo=column();TextView title=text("NIYVEXA",18,true,VIOLET);title.setLetterSpacing(.14f);logo.addView(title);logo.addView(text(t("Intent first"),10,false,MUTED));
        head.addView(logo,new LinearLayout.LayoutParams(0,-2,1));
        Spinner languages=new Spinner(this);String[] languageNames={"العربية","English","Français","Español"};
        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,languageNames);languages.setAdapter(adapter);
        languages.setSelection(Arrays.asList("ar","en","fr","es").indexOf(lang));
        head.addView(languages,lp(125,42));languages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            boolean first=true; public void onNothingSelected(AdapterView<?> parent){}
            public void onItemSelected(AdapterView<?> parent,View view,int position,long id){String next=new String[]{"ar","en","fr","es"}[position];if(first){first=false;return;}if(!next.equals(lang)){lang=next;saveState();buildRoot();}}
        });
        root.addView(head);
        TextView banner=text(t("Fictional profiles only. Everything stays on this device; no real user receives likes or messages."),11,false,0xffd8d0f2);
        banner.setBackground(shape(0xff211f31,12,0xff594f79));pad(banner,13,11);LinearLayout.LayoutParams bp=lp(-1,-2);bp.setMargins(dp(14),dp(0),dp(14),dp(8));root.addView(banner,bp);
        HorizontalScrollView nscroll=new HorizontalScrollView(this);nscroll.setHorizontalScrollBarEnabled(false);nav=row();nav.setPadding(dp(8),dp(1),dp(8),dp(8));
        String[] tabs={"Discover","Preferences","Matches","Messages","Plus","Safety"};
        for(String item:tabs){Button b=button(t(item),()->{tab=item;buildRoot();},item.equals(tab));LinearLayout.LayoutParams p=lp(-2,42);p.setMargins(dp(3),0,dp(3),0);nav.addView(b,p);}
        nscroll.addView(nav);root.addView(nscroll);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);body=column();pad(body,14,5);scroll.addView(body);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        addText(body,t("DEMO • 18+")+"  ·  "+t("Preview only — not a public service yet"),10,true,0xff93b9a9,3);
        gap(body,8);
        switch(tab){
            case "Discover": buildDiscover();break;
            case "Preferences":buildPreferences();break;
            case "Matches":buildMatches();break;
            case "Messages":buildMessages();break;
            case "Plus":buildBusiness();break;
            case "Safety":buildSafety();break;
        }
        addText(body,"NIYVEXA  ·  "+t("No data leaves your phone."),10,false,MUTED,14);
        setContentView(root);
    }

    private Spinner spinner(String[] options,String selected,Pick pick){
        Spinner s=new Spinner(this);String[] labels=new String[options.length];for(int i=0;i<options.length;i++)labels[i]=t(options[i]);
        ArrayAdapter<String> ad=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,labels);s.setAdapter(ad);
        int pos=Arrays.asList(options).indexOf(selected);if(pos<0)pos=0;s.setSelection(pos);s.setBackground(shape(PANEL2,10,0xff41475a));s.setPadding(dp(7),0,dp(7),0);
        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){boolean first=true;public void onNothingSelected(AdapterView<?> p){}public void onItemSelected(AdapterView<?> p,View v,int position,long id){if(first){first=false;return;}pick.onPick(options[position]);}});
        return s;
    }
    private LinearLayout field(LinearLayout parent,String label,View input){
        LinearLayout box=column();addText(box,t(label),11,true,0xffc5cadb,0);LinearLayout.LayoutParams ip=lp(-1,-2);ip.topMargin=dp(5);box.addView(input,ip);
        LinearLayout.LayoutParams bp=lp(-1,-2);bp.bottomMargin=dp(11);parent.addView(box,bp);return box;
    }
    private EditText input(String value,int inputType){
        EditText e=new EditText(this);e.setText(value);e.setTextSize(14);e.setTextColor(TEXT);e.setSingleLine(true);e.setInputType(inputType);e.setPadding(dp(11),dp(5),dp(11),dp(5));e.setBackground(shape(0xff11141c,10,0xff3b4052));return e;
    }
    private LinearLayout formRow(LinearLayout parent){
        LinearLayout r=row();r.setGravity(Gravity.TOP);parent.addView(r,new LinearLayout.LayoutParams(-1,-2));return r;
    }
    private void buildDiscover(){
        addText(body,t("Mutual-intent discovery"),24,true,TEXT,0);
        addText(body,t("Pick your intentions and boundaries before browsing. This is an offline demo, not a live dating service."),12,false,MUTED,7);
        LinearLayout f=card(t("Filter profiles"),t("Who do you want to meet?"));
        field(f,"Who do you want to meet?",spinner(new String[]{"Everyone","Men","Women"},filterGender,v->{filterGender=v;}));
        LinearLayout r=formRow(f);
        LinearLayout left=column(),right=column();r.addView(left,new LinearLayout.LayoutParams(0,-2,1));r.addView(right,new LinearLayout.LayoutParams(0,-2,1));pad(right,8,0);
        EditText min=input(""+minAge,2),max=input(""+maxAge,2);field(left,"Minimum age",min);field(right,"Maximum age",max);
        EditText dist=input(""+maxDistance,2);field(f,"Max distance (km)",dist);
        field(f,"Intent",spinner(new String[]{"Any intent","Friendship","Social connection","Serious relationship","Exploration"},filterIntent,v->filterIntent=v));
        field(f,"City",spinner(new String[]{"Any city","Guelma","Constantine","Algiers","Setif"},filterCity,v->filterCity=v));
        addButton(f,"Apply filters",()->{minAge=bounded(min,18,99,18);maxAge=Math.max(minAge,bounded(max,18,99,35));maxDistance=bounded(dist,1,1000,100);saveState();buildRoot();},true);
        addButton(f,"Reset filters",()->{filterGender="Everyone";filterIntent="Any intent";filterCity="Any city";minAge=18;maxAge=35;maxDistance=100;saveState();buildRoot();},false);
        List<Profile> valid=new ArrayList<>();for(Profile p:profiles)if(eligible(p))valid.add(p);
        if(valid.isEmpty()){LinearLayout empty=card(null,null);addText(empty,t("Nothing fits yet. Try a wider age range, another intent or a longer distance."),13,false,MUTED,0);}
        else for(Profile p:valid) buildProfileCard(p);
    }
    private int bounded(EditText e,int min,int max,int fallback){try{return Math.max(min,Math.min(max,Integer.parseInt(e.getText().toString().trim())));}catch(Exception ex){return fallback;}}
    private boolean eligible(Profile p){
        if(blocked.contains(p.id))return false;
        if(p.age<minAge||p.age>maxAge||p.distance>maxDistance)return false;
        if(!filterGender.equals("Everyone")&&!filterGender.equals(p.gender))return false;
        if(!filterCity.equals("Any city")&&!filterCity.equals(p.city))return false;
        if(!filterIntent.equals("Any intent")&&!Arrays.asList(p.intents).contains(filterIntent))return false;
        if(Collections.disjoint(myIntents,Arrays.asList(p.intents)))return false;
        if(!Arrays.asList(p.looking).contains(gender))return false;
        if(age<p.minAge||age>p.maxAge||p.distance>p.maxDistance)return false;
        if(!looking.equals("Everyone")&&!looking.equals(p.gender))return false;
        return true;
    }
    private void buildProfileCard(Profile p){
        LinearLayout c=card(p.name+", "+p.age,t(p.city)+" · "+p.distance+" "+t("away"));
        TextView badge=text(t("Fictional demo profile"),10,true,0xffbdb4f4);badge.setBackground(shape(0xff28253a,999,0xff4d456b));pad(badge,9,4);c.addView(badge,lp(-2,-2));
        addText(c,t(p.bio),12,false,MUTED,8);
        StringBuilder tags=new StringBuilder();for(String intent:p.intents){if(tags.length()>0)tags.append(" · ");tags.append(t(intent));}
        addText(c,tags.toString(),11,true,0xff93b9a9,8);
        if(p.mutual){addText(c,t("Mutual demo interest"),10,true,0xff93b9a9,6);}
        addButton(c,likes.contains(p.id)?t("Interested"):t("Show interest"),()->{
            likes.add(p.id);saveState();if(p.mutual)toast("Local demo match created; no person was notified.");else toast("Interested");buildRoot();
        },true);
        addButton(c,"⊘  "+t("Block"),()->new AlertDialog.Builder(this).setMessage(t("Block this fictional profile on this device?")).setNegativeButton(t("Cancel"),(d,w)->{}).setPositiveButton(t("Block"),(d,w)->{blocked.add(p.id);saveState();toast("Profile blocked locally.");buildRoot();}).show(),false);
        addButton(c,"⚑  "+t("Report"),()->{reported.add(p.id);saveState();toast("Demo report saved only on this device; nothing was sent.");},false);
    }

    private void buildPreferences(){
        addText(body,t("Your profile & boundaries"),23,true,TEXT,0);
        addText(body,t("Fictional profiles only. Everything stays on this device; no real user receives likes or messages."),12,false,MUTED,6);
        LinearLayout c=card(t("Your profile & boundaries"),null);
        EditText n=input(name,android.text.InputType.TYPE_CLASS_TEXT);field(c,"Your display name",n);
        EditText a=input(""+age,2);field(c,"Your age (18+)",a);
        field(c,"Your gender",spinner(new String[]{"Man","Woman"},gender,v->gender=v));
        field(c,"Your city",spinner(new String[]{"Guelma","Constantine","Algiers","Setif"},city,v->city=v));
        field(c,"Who do you want to meet?",spinner(new String[]{"Everyone","Men","Women"},looking,v->looking=v));
        EditText d=input(""+maxDistance,2);field(c,"Maximum distance you accept (km)",d);
        EditText min=input(""+minAge,2),max=input(""+maxAge,2);LinearLayout r=formRow(c);LinearLayout l=column(),rr=column();r.addView(l,new LinearLayout.LayoutParams(0,-2,1));r.addView(rr,new LinearLayout.LayoutParams(0,-2,1));pad(rr,8,0);field(l,"People aged at least",min);field(rr,"People aged at most",max);
        addText(c,t("Choose your intentions"),12,true,0xffc5cadb,3);
        for(String intent:new String[]{"Friendship","Social connection","Serious relationship","Exploration"}){
            CheckBox cb=new CheckBox(this);cb.setText(t(intent));cb.setTextColor(TEXT);cb.setTextSize(13);cb.setButtonTintList(android.content.res.ColorStateList.valueOf(VIOLET));cb.setChecked(myIntents.contains(intent));c.addView(cb);
            cb.setOnCheckedChangeListener((v,checked)->{if(checked)myIntents.add(intent);else myIntents.remove(intent);});
        }
        addButton(c,"Save preferences",()->{
            int nextAge=bounded(a,18,99,age);if(nextAge<18||myIntents.isEmpty()){toast("Please enter an age from 18 to 99 and select at least one intention.");return;}
            name=n.getText().toString().trim();if(name.isEmpty())name="Guest";age=nextAge;maxDistance=bounded(d,1,1000,100);minAge=bounded(min,18,99,18);maxAge=Math.max(minAge,bounded(max,18,99,35));
            saveState();((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(n.getWindowToken(),0);toast("Preferences saved on this device.");buildRoot();
        },true);
    }

    private boolean matched(Profile p){return likes.contains(p.id)&&p.mutual&&!blocked.contains(p.id);}
    private List<Profile> matches(){List<Profile> out=new ArrayList<>();for(Profile p:profiles)if(matched(p))out.add(p);return out;}
    private void buildMatches(){
        addText(body,t("Matches"),24,true,TEXT,0);
        addText(body,t("Your matches exist only in this demo. A match is simulated when a profile is marked as mutually interested."),12,false,MUTED,7);
        List<Profile> list=matches();
        if(list.isEmpty()){LinearLayout c=card(null,null);addText(c,t("No local matches yet."),15,true,TEXT,0);addText(c,t("Your matches exist only in this demo. A match is simulated when a profile is marked as mutually interested."),12,false,MUTED,6);}
        else for(Profile p:list){LinearLayout c=card(p.name+", "+p.age,t(p.city)+" · "+t("Mutual demo interest"));addButton(c,t("Open demo chat"),()->{chatId=p.id;tab="Messages";buildRoot();},true);}
    }
    private JSONArray readChat(String id){try{return new JSONArray(prefs.getString("chat_"+id,"[]"));}catch(Exception e){return new JSONArray();}}
    private void buildMessages(){
        addText(body,t("Messages"),24,true,TEXT,0);
        addText(body,t("Messages stay on this device. No other person can read or reply to them."),12,false,MUTED,7);
        List<Profile> list=matches();
        if(list.isEmpty()){LinearLayout c=card(null,null);addText(c,t("No matches are available for chatting yet."),14,true,TEXT,0);return;}
        if(chatId.isEmpty()||list.stream().noneMatch(p->p.id.equals(chatId)))chatId=list.get(0).id;
        LinearLayout pick=card(t("Open demo chat"),null);
        for(Profile p:list)addButton(pick,p.name,()->{chatId=p.id;buildRoot();},p.id.equals(chatId));
        Profile selected=null;for(Profile p:list)if(p.id.equals(chatId))selected=p;if(selected==null)return;
        LinearLayout c=card(selected.name,t("Fictional demo profile"));
        JSONArray msgs=readChat(chatId);
        if(msgs.length()==0)addText(c,t("Messages stay on this device. No other person can read or reply to them."),11,false,MUTED,0);
        for(int i=0;i<msgs.length();i++){String m=msgs.optString(i,"");TextView bubble=text(m,13,false,TEXT);bubble.setBackground(shape(PANEL2,12,0xff41475a));pad(bubble,11,9);LinearLayout.LayoutParams bp=lp(-1,-2);bp.topMargin=dp(7);c.addView(bubble,bp);}
        EditText msg=input("",android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES|android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);msg.setHint(t("Write a message…"));
        c.addView(msg,lp(-1,-2));addButton(c,t("Send"),()->{
            String value=msg.getText().toString().trim();if(value.isEmpty())return;
            JSONArray all=readChat(chatId);all.put(value.substring(0,Math.min(500,value.length())));prefs.edit().putString("chat_"+chatId,all.toString()).apply();toast("Message saved locally.");((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(msg.getWindowToken(),0);buildRoot();
        },true);
    }

    private void buildBusiness(){
        addText(body,t("Business model"),24,true,TEXT,0);
        addText(body,t("No ad network or payment system is connected. This version earns no money."),12,false,MUTED,7);
        LinearLayout c=card(t("Free core"),t("Discovery, mutual preference filters and fictional test profiles."));
        label(c,t("Available now — demo only"),0xff93b9a9);
        LinearLayout p=card("NIYVEXA PLUS",t("Optional advanced filters could be paid once real accounts, a secure server and a lawful payment provider are ready."));
        label(p,t("Planned — not active"),0xffd7cdf5);
        LinearLayout ad=card(t("Limited ads — not active"),t("No ad network or payment system is connected. This version earns no money."));
        label(ad,t("Planned — not active"),0xffd7cdf5);
        LinearLayout note=card(null,null);addText(note,t("No data leaves your phone."),13,true,TEXT,0);
    }
    private void label(LinearLayout c,String value,int color){TextView v=text(value,10,true,color);v.setBackground(shape(PANEL2,999,0xff41475a));pad(v,9,5);LinearLayout.LayoutParams p=lp(-2,-2);p.topMargin=dp(9);c.addView(v,p);}
    private void buildSafety(){
        addText(body,t("Safety & local data"),24,true,TEXT,0);
        addText(body,t("Fictional profiles only. Everything stays on this device; no real user receives likes or messages."),12,false,MUTED,7);
        LinearLayout stats=card(t("Safety & local data"),null);
        addText(stats,t("Blocked profiles")+": "+blocked.size(),13,true,TEXT,2);
        addText(stats,t("Saved local reports")+": "+reported.size(),13,true,TEXT,6);
        addButton(stats,t("Delete all demo data"),()->new AlertDialog.Builder(this).setMessage(t("Delete local data?")).setNegativeButton(t("Cancel"),(d,w)->{}).setPositiveButton(t("Delete"),(d,w)->{String keepLang=lang; prefs.edit().clear().putString("lang",keepLang).apply();likes.clear();blocked.clear();reported.clear();myIntents.clear();myIntents.add("Friendship");myIntents.add("Serious relationship");name="Guest";age=19;gender="Man";city="Guelma";looking="Everyone";minAge=18;maxAge=35;maxDistance=100;filterGender="Everyone";filterIntent="Any intent";filterCity="Any city";chatId="";loadState();toast("All local demo data was deleted.");buildRoot();}).show(),false);
        LinearLayout note=card(t("Safety & local data"),t("This is an offline prototype. Block and report actions affect only this device and are not sent to a moderation team."));
    }
    private void toast(String message){Toast.makeText(this,t(message),Toast.LENGTH_LONG).show();}
}