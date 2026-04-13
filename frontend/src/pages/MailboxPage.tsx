import React, { useState } from 'react';
import InboxList from '../components/emails/InboxList';
import EmailReader from '../components/emails/EmailReader';
import { useMailbox } from '../hooks/useMailbox';
import { useThreadDetail } from '../hooks/useThreadDetail';

/**
 * Generic mailbox page shared by all folder routes:
 *   /inbox   → folder="inbox"
 *   /sent    → folder="sent"
 *   /drafts  → folder="drafts"
 *   /trash   → folder="trash"
 */
export default function MailboxPage({ folder }: { folder: string }) {
	const [selectedThreadId, setSelectedThreadId] = useState<string | null>(null);

	const {
		threads,
		isLoadingThreads,
		activeTab,
		setActiveTab,
		showUnreadOnly,
		setShowUnreadOnly,
		hasMore,
		isLoadingMore,
		loadMore,
		updateThreadLocally,
		removeThreadLocally
	} = useMailbox(folder);

	const {
		selectedThreadData,
		isLoadingDetail,
		updateThreadDataLocally,
		clearThreadLocally
	} = useThreadDetail(selectedThreadId);

	const handleThreadUpdated = (updatedData) => {
        const fixedData = updateThreadDataLocally(updatedData);
        updateThreadLocally(fixedData);
    };

    const handleThreadDeleted = (deletedId) => {
        clearThreadLocally();
        setSelectedThreadId(null);
        removeThreadLocally(deletedId);
    };

	return (
		<>
			<InboxList
				folder={folder}
				threads={threads}
				isLoading={isLoadingThreads}
				selectedThreadId={selectedThreadId}
				onSelectThread={setSelectedThreadId}
				activeTab={activeTab}
				onTabChange={setActiveTab}
				showUnreadOnly={showUnreadOnly}
				onToggleUnread={() => setShowUnreadOnly(!showUnreadOnly)}
				onLoadMore={loadMore}
				hasMore={hasMore}
				isLoadingMore={isLoadingMore}
			/>
			<EmailReader
				folder={folder}
				selectedThread={selectedThreadData}
				isLoading={isLoadingDetail}
				onThreadUpdated={handleThreadUpdated}
				onThreadDeleted={handleThreadDeleted}
			/>
		</>
	);
}
