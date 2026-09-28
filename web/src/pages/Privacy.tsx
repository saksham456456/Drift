import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';

const Privacy: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex flex-col transition-colors">
      <div className="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 flex items-center p-4">
        <button onClick={() => navigate(-1)} className="mr-4 text-gray-700 dark:text-gray-200 hover:text-gray-900 dark:hover:text-white transition-colors">
          <ArrowLeft className="w-6 h-6" />
        </button>
        <h1 className="text-xl font-semibold text-gray-900 dark:text-white">Privacy Policy</h1>
      </div>
      
      <div className="flex-1 overflow-y-auto p-6 text-gray-700 dark:text-gray-300">
        <div className="max-w-3xl mx-auto space-y-6">
          <section>
            <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-3">1. Data Collection</h2>
            <p>
              When you use Drift, we collect information you provide directly to us, such as your display name, username, and authentication details. We also collect the messages you send through the platform.
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-3">2. Data Usage</h2>
            <p>
              We use the collected data to provide, maintain, and improve our services. Your information helps us facilitate communication between users and ensure the security of our platform.
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-3">3. Data Storage</h2>
            <p>
              Your data is stored securely on our servers. We implement reasonable security measures to protect your personal information from unauthorized access, alteration, or destruction.
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-3">4. Third Parties (Firebase)</h2>
            <p>
              Drift utilizes Firebase (a Google service) for authentication, database storage, and hosting. By using Drift, you also agree to Firebase's privacy policies regarding data processing and storage.
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-3">5. User Rights</h2>
            <p>
              You have the right to access, correct, or delete your personal data. You can delete your account at any time through the app's Settings menu, which will remove your identifying data from our active databases.
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-3">6. Contact Information</h2>
            <p>
              If you have any questions about this Privacy Policy or our data practices, please contact us at privacy@drift.app.
            </p>
          </section>
        </div>
      </div>
    </div>
  );
};

export default Privacy;
