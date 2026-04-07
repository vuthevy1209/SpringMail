import mailService from '../service/mailService';
import InboxList from '../components/emails/InboxList';
import EmailReader from '../components/emails/EmailReader'; // Assuming rename from ReaderComposer
import { useState, useEffect } from 'react';

export default function InboxPage() {
	const [emails, setEmails] = useState([]);
	const [isLoadingEmails, setIsLoadingEmails] = useState(false);
	const [selectedEmailId, setSelectedEmailId] = useState(null);
	const [activeTab, setActiveTab] = useState('primary');

	useEffect(() => {
		const fetchEmails = async () => {
			setIsLoadingEmails(true);
			try {
				const data = await mailService.fetchEmails('inbox', activeTab);
				setEmails(data);
			} catch (error) {
				console.error("Failed to fetch emails:", error);
			} finally {
				setIsLoadingEmails(false);
			}
		};
		fetchEmails();
	}, [activeTab]);

	return (
		<>
			<InboxList
				emails={emails}
				isLoading={isLoadingEmails}
				selectedEmailId={selectedEmailId}
				onSelectEmail={setSelectedEmailId}
				activeTab={activeTab}
				onTabChange={setActiveTab}
			/>
			<EmailReader
				emails={emails}
				selectedThreadId={selectedEmailId}
			/>
		</>
	);
}
