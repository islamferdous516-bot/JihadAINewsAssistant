# Jihad AI News Assistant — Android MVP

## কী আছে
- RSS থেকে খবর সংগ্রহ (BBC বাংলা ও DW বাংলা ফিড)
- SQLite-এ স্থানীয়ভাবে খবর সংরক্ষণ
- অফলাইনে সংরক্ষিত খবর পড়া ও শিরোনাম/বর্ণনায় সার্চ
- WorkManager দিয়ে নেটওয়ার্ক থাকলে ৬ ঘণ্টা অন্তর ব্যাকগ্রাউন্ড সিঙ্ক
- সাধারণ extractive offline summary

## গুরুত্বপূর্ণ সীমাবদ্ধতা
এই MVP-তে জেনারেটিভ LLM বা AI API এখনো যুক্ত নয়। `AiService`-এর সারাংশটি স্থানীয় extractive fallback; এটি প্রকৃত LLM সারাংশ নয়। পরবর্তী ধাপে API key নিরাপদভাবে সংরক্ষণ, AI provider নির্বাচন এবং চাইলে ছোট on-device model যোগ করতে হবে। API key কখনো GitHub-এ commit করবেন না।

## ফোন দিয়ে APK তৈরি
1. GitHub-এ নতুন repository তৈরি করুন: `JihadAINewsAssistant`
2. এই ZIP-এর সব ফাইল repository-র root-এ upload করুন (ZIP ফাইলটি নিজে নয়; Extract করে ফাইলগুলো upload করুন)।
3. GitHub repository → Actions → `Build Android APK` → Run workflow চাপুন।
4. Build শেষ হলে workflow run-এর নিচে Artifacts থেকে `Jihad-AI-News-Assistant-debug` ডাউনলোড করুন।
5. ZIP artifact Extract করুন। `app-debug.apk` ফোনে খুলে Install দিন। প্রয়োজন হলে Android Settings-এ ওই browser/file manager-এর “Install unknown apps” অনুমতি দিন।

## Build requirements
- JDK 17
- Android SDK Platform 35
- Gradle 8.9
- Android Gradle Plugin 8.7.3

## নোট
- Android WorkManager periodic task-এর সর্বনিম্ন interval ১৫ মিনিট; এখানে ৬ ঘণ্টা নির্ধারণ করা হয়েছে। Android ব্যাটারি ব্যবস্থাপনার কারণে সিঙ্ক ঠিক নির্দিষ্ট মিনিটে নাও হতে পারে।
- RSS feed পরিবর্তন বা বন্ধ হতে পারে। প্রয়োজনে `NewsRepository.kt`-এ ফিড URL বদলান।
- প্রথমবার খবর আনতে ইন্টারনেট লাগবে। এরপর সংরক্ষিত খবর অফলাইনে পড়া যাবে।
- এটি debug APK; Play Store release-এর জন্য signing configuration ও release build দরকার।
