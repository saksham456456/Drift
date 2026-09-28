import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';

const Privacy: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 transition-colors">
      <header className="sticky top-0 z-10 flex items-center px-4 py-3 bg-white dark:bg-gray-800 shadow-sm border-b border-gray-200 dark:border-gray-700">
        <button onClick={() => navigate(-1)} className="p-2 mr-2 rounded-full hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-600 dark:text-gray-300">
          <ArrowLeft size={22} />
        </button>
        <h1 className="text-lg font-semibold text-gray-900 dark:text-white">Privacy Policy</h1>
      </header>

      <main className="max-w-2xl mx-auto px-6 py-8">
        <p className="text-sm text-gray-500 dark:text-gray-400 mb-6">Last updated: September 2026</p>

        <section className="mb-8">
          <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-3">1. Information We Collect</h2>
          <p className="text-gray-700 dark:text-gray-300 leading-relaxed mb-2">When you create a Drift account, we collect your chosen username, display name, and a securely hashed password. We do not collect your real name, email address, phone number, or any other personally identifiable information.</p>
          <p className="text-gray-700 dark:text-gray-300 leading-relaxed">We also collect basic usage data such as message timestamps and online/offline status to provide our core messaging functionality.</p>
        </section>

        <section className="mb-8">
          <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-3">2. How We Use Your Information</h2>
          <p className="text-gray-700 dark:text-gray-300 leading-relaxed">Your information is used solely to provide the Drift messaging service. This includes delivering messages between users, maintaining your chat history, and displaying your online status to your contacts. We do not sell, rent, or share your personal information with third parties for marketing purposes.</p>
        </section>

        <section className="mb-8">
          <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-3">3. Data Storage & Security</h2>
          <p className="text-gray-700 dark:text-gray-300 leading-relaxed">Your data is stored securely using Google Firebase infrastructure, which employs industry-standard encryption and security practices. Messages are stored in Firebase Realtime Database with access controlled by security rules that ensure only authorized participants can read conversation data.</p>
        </section>

        <section className="mb-8">
          <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-3">4. Third-Party Services</h2>
          <p className="text-gray-700 dark:text-gray-300 leading-relaxed">Drift uses the following third-party services:</p>
          <ul className="list-disc pl-6 mt-2 text-gray-700 dark:text-gray-300 space-y-1">
            <li>Firebase Authentication — for secure user login</li>
            <li>Firebase Realtime Database — for message storage and delivery</li>
            <li>Firebase Cloud Messaging — for push notifications</li>
          </ul>
          <p className="text-gray-700 dark:text-gray-300 leading-relaxed mt-2">These services are governed by Google's Privacy Policy.</p>
        </section>

        <section className="mb-8">
          <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-3">5. Your Rights</h2>
          <p className="text-gray-700 dark:text-gray-300 leading-relaxed">You have the right to access, modify, or delete your personal data at any time through the Settings page of the application. You can delete your entire account, which will permanently remove your profile, username, and associated data from our systems.</p>
        </section>

        <section className="mb-8">
          <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-3">6. Contact</h2>
          <p className="text-gray-700 dark:text-gray-300 leading-relaxed">If you have any questions or concerns about this Privacy Policy, please contact us through the application's support channels.</p>
        </section>
      </main>
    </div>
  );
};

export default Privacy;
