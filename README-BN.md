# Jihad AI News Assistant

এটি Android-এর জন্য একটি প্রাথমিক MVP (Minimum Viable Product)।

## কী কাজ করে
- BBC বাংলা RSS থেকে অনলাইনে খবর আনার চেষ্টা করে।
- সর্বশেষ পাওয়া খবর ফোনে ক্যাশ হিসেবে রাখে।
- ইন্টারনেট না থাকলে আগে সংরক্ষিত খবর দেখায়।
- ক্যাশ করা খবরের শিরোনাম ও বিবরণে সার্চ করা যায়।
- খবর আলাদাভাবে সেভ করে পরে অফলাইনে দেখা যায়।
- মূল সংবাদ লিংক ব্রাউজারে খোলে।

## গুরুত্বপূর্ণ সীমাবদ্ধতা
- এই সংস্করণে কোনো প্রকৃত LLM/AI মডেল বা AI-লিখিত সারাংশ নেই।
- বিবরণ RSS ফিড থেকে আসে; এটি সম্পূর্ণ সংবাদপাঠ নয়।
- অফলাইন সুবিধা শুধু আগে ডাউনলোড/সেভ করা খবরের জন্য।
- RSS উৎস ভবিষ্যতে পরিবর্তিত হতে পারে।

## Build
Codemagic-এ GitHub repository সংযুক্ত করে root-এ থাকা `codemagic.yaml` ব্যবহার করুন।
Build artifact: `app/build/outputs/apk/debug/app-debug.apk`

স্থানীয়ভাবে:
```bash
chmod +x gradlew
./gradlew assembleDebug
```

## Project
- Package: `com.jihad.ai.newsassistant`
- Min Android: 6.0 (API 23)
- Compile/Target SDK: 35
- Java: 17
- Gradle: 8.9
