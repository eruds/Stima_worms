i.Strategi Greedy yang Digunakan

Program ini menggunakan kombinasi beberapa strategi untuk memaksimalkan potensi kemenangan, tergantung gameState pada saat tertentu.

Kombinasi strategi yang digunakan adalah strategi Swarm Attack, Attack Closest Enemy, dan Hunt for Healthpack, yang penggunaannya disesuaikan dengan gameState. Swarm Attack menyerang hanya satu Worm lawan, Closest Enemy menyerang Worm lawan terdekat, dan Hunt for Healthpack mencari healthpack terdekat. Penggunaan kombinasi strategi ini adalah untuk memaksimalkan potensi menang dengan memanfaatkan kondisi gameState yang selalu berubah pada setiap ronde.

Strategi diurutkan dengan prioritas Swarm Attack, Attack Closest Enemy, kemudian Hunt for Healthpack. Tujuan dari algoritma ini adalah menentukan apakah kita harus menyerang satu worm, menyerang worm terdekat, atau mencari healthpack.

Kondisi gamestate yang dipertimbangkan adalah sebagai berikut :

> Apabila tidak ada worm musuh yang terlalu dekat, maka atur agar worm player untuk mendekati atau menyerang worm target ( Worm yang dijadikan sasaran utama dalam strategi Swarm Attack ).

> Jika ada worm yang terlalu dekat, worm player akan mulai menyerang worm lawan yang terlalu dekat, sementara worm player lainnya tetap memfokuskan serangan terhadap worm target.

> Jika health point yang dimiliki worm player berada diantara range 30 dan 50, dan masih ada healthpack yang tersedia di map, maka worm akan memilih untuk mencari healthpack terlebih dahulu.

# ii. Persyaratan lingkungan

Instal Java SE Development Kit 8 untuk lingkungan Anda dari link ini: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html

Pastikan variabel sistem JAVA_HOME diatur, tutorial Windows 10 di sini: https://www.mkyong.com/java/how-to-set-java_home-on-windows-10/

Bot membutuhkan game runner dan game engine yang tersedia pada starter pack Entelenct Challenge Worms 2019 pada link berikut : https://github.com/EntelectChallenge/2019-Worms/releases/tag/2019.3.2

# iii. Cara Menjalankan Program

Untuk menjalankan bot, user harus menyesuaikan pengaturan pada file game-runner-config.json untuk mengarahkan game runner kepada folder yang memuat bot.json metadata dan file Kelompok20_American_Standard.jar

Kemudian jalankan file "run.bat" di windows atau file "run.sh" untuk unix.

# iv. Author

Bot ini dibuat oleh Kelompok 20, dengan nama American Standard sebagai pemenuhan tugas besar 1 Strategi Algoritma Semester 4 IF ITB 2020/2021.
Berikut adalah nama nama anggota kelompok :

1. Mahameru Ds
2. Ilyasa Salafi Putra Jamal
3. R. B. Wishnumurti
