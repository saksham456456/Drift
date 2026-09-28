import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';

const Terms: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex flex-col transition-colors">
      <div className="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 flex items-center p-4">
        <button onClick={() => navigate(-1)} className="mr-4 text-gray-700 dark:text-gray-200 hover:text-gray-900 dark:hover:text-white transition-colors">
          <ArrowLeft className="w-6 h-6" />
        </button>
        <h1 className="text-xl font-semibold text-gray-900 dark:text-white">Terms of Service</h1>
      </div>
      
      <div className="flex-1 overflow-y-auto p-6 text-gray-700 dark:text-gray-300">
        <div className="max-w-3xl mx-auto space-y-6">
          <section>
            <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-3">1. Account Creation</h2>
            <p>
              By creating an account on Drift, you agree to provide accurate, current, and complete information. You are responsible for maintaining the confidentiality of your account credentials.
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-3">2. Acceptable Use</h2>
            <p>
              You agree not to use Drift to send spam, harass others, distribute malware, or engage in any illegal activities. We reserve the right to suspend or terminate accounts that violate these guidelines.
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-3">3. User Content</h2>
            <p>
              You retain ownership of the messages and content you send through Drift. However, by using the service, you grant us a license to process and transmit your content as necessary to operate the application.
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-3">4. Termination</h2>
            <p>
              We may terminate or suspend your access to Drift at any time, without prior notice or liability, for any reason, including without limitation if you breach these Terms of Service.
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-3">5. Disclaimers</h2>
            <p>
              Drift is provided "as is" without any warranties, express or implied. We do not guarantee that the service will be uninterrupted, secure, or error-free.
            </p>
          </section>

          <section>
            <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-3">6. Limitation of Liability</h2>
            <p>
              In no event shall Drift, its developers, or affiliates be liable for any indirect, incidental, special, consequential, or punitive damages arising out of your use of the service.
            </p>
          </section>
        </div>
      </div>
    </div>
  );
};

export default Terms;
