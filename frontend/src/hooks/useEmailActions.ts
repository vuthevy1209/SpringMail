import { useState } from 'react';
import mailService from '../services/mailService';
import { Thread } from '../types/mail';

export function useEmailActions(
    thread: Thread | null, 
    folder: string, 
    onThreadUpdated?: (data: Thread) => void, 
    onThreadDeleted?: (id: string) => void
) {
    const [isConfirmModalOpen, setIsConfirmModalOpen] = useState<boolean>(false);
    const [actionToConfirm, setActionToConfirm] = useState<string | null>(null);

    const handleActionClick = (actionName: string) => {
        setActionToConfirm(actionName);
        setIsConfirmModalOpen(true);
    };

    const handleConfirmAction = async () => {
        if (!actionToConfirm || !thread) return;
        
        try {
            let updatedData: Thread | null = null;
            let shouldDelete = false;

            if (actionToConfirm === 'Archive') {
                updatedData = await mailService.modifyThread(thread.id, [], ["INBOX"]) as Thread;
                shouldDelete = true;
            } else if (actionToConfirm === 'Report spam') {
                let removeLabels = new Set(["INBOX"]);
                if (folder) removeLabels.add(folder.toUpperCase());
                removeLabels.delete("SPAM");
                
                updatedData = await mailService.modifyThread(thread.id, ["SPAM"], Array.from(removeLabels)) as Thread;
                shouldDelete = true;
            } else if (actionToConfirm === 'Delete') {
                await mailService.trashThread(thread.id);
                shouldDelete = true;
            } else if (actionToConfirm === 'Mark as read') {
                updatedData = await mailService.modifyThread(thread.id, [], ["UNREAD"]) as Thread;
            } else if (actionToConfirm === 'Mark as unread') {
                updatedData = await mailService.modifyThread(thread.id, ["UNREAD"], []) as Thread;
            } else if (actionToConfirm.startsWith('Move to')) {
                const folderName = actionToConfirm.replace('Move to ', '');
                const targetLabelId = folderName.toUpperCase();
                
                let removeLabels = new Set(["INBOX"]);
                if (folder) removeLabels.add(folder.toUpperCase());
                removeLabels.delete(targetLabelId);
                
                updatedData = await mailService.modifyThread(thread.id, [targetLabelId], Array.from(removeLabels)) as Thread;
                shouldDelete = true;
            }

            if (shouldDelete && onThreadDeleted) {
                onThreadDeleted(thread.id);
            } else if (updatedData && onThreadUpdated) {
                onThreadUpdated(updatedData);
            }
        } catch (error) {
            console.error(`Failed to execute ${actionToConfirm}`, error);
        } finally {
            setIsConfirmModalOpen(false);
            setActionToConfirm(null);
        }
    };

    const handleCancelAction = () => {
        setIsConfirmModalOpen(false);
        setActionToConfirm(null);
    };

    return {
        isConfirmModalOpen,
        actionToConfirm,
        handleActionClick,
        handleConfirmAction,
        handleCancelAction
    };
}
