MOCAP UI (add-on) - 1.21.11 Fabric
===================================
Ye original "Motion Capture" (Mocap) mod ke SAATH chalta hai (usko hatana nahi hai).
Ye sirf ek client-side UI hai: buttons dabane par andar se asli /mocap commands chalte hain,
isliye Mocap ke saare features same rehte hain.

Mods folder me chahiye:  Fabric API + Mocap (1.21.11 wala) + mocap-ui-1.0.0.jar
Key: M  (Controls me "Mocap UI" category se badal sakte ho)
Commands chalane ke liye op/cheats permission zaroori hai (singleplayer me cheats ON).

BUILD
-----
1) JDK 21 install karo.
2) https://fabricmc.net/develop pe 1.21.11 select karo, wahan jo loader/fabric-api/loom
   versions dikhen unhe gradle.properties me daal do.
3) Sabse aasan: us site se "Template mod generator" zip lo, usme se gradlew, gradlew.bat
   aur gradle/ folder is project me copy kar do. (Ya Gradle install ho to: gradle wrapper)
4) Terminal me project folder me:   gradlew build        (Windows)   ./gradlew build   (Linux/Mac)
5) Jar milegi:  build/libs/mocap-ui-1.0.0.jar
   (IntelliJ me folder open karke "build" task chalana bhi chalega)

UI KAISE USE KARE
-----------------
Actors tab : 4 slots. Har slot: ON/OFF, recording name, actor name, skin type (Default/Player/File/Mineskin),
             skin value (player name / file name / mineskin link), Play.
             "Play ALL" = sare ON actors ek saath, synced (ek scene banake).
Recording  : Start/Stop/Save recording, recordings manage (info/rename/copy/remove), scenes manage.
Settings   : sab Mocap settings ke ON/OFF buttons + koi bhi setting value se set karne ka row.
Neeche UI ke andar Mocap ka output (last 5 lines) dikhta hai.

BINA INSTALL KIYE JAR BANANE KA TARIKA (GitHub)
-----------------------------------------------
1) github.com pe free account -> New repository.
2) Is folder ki saari files (".github" folder samet) us repo me upload karo.
3) Repo ke "Actions" tab me "build" chalne do (ya "Run workflow" dabao).
4) Complete hone par us run ke "Artifacts" me mocap-ui-jar download karo -> usme jar hai.
(Pehle gradle.properties me versions fabricmc.net/develop se match kar lena.)
