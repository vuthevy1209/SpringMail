import React from 'react';
import Skeleton, { SkeletonTheme } from 'react-loading-skeleton';
import 'react-loading-skeleton/dist/skeleton.css';

const ThreadSkeleton = () => (
    <div className="px-5 py-4 border-b border-whisper/50 flex gap-3">
        {/* Avatar */}
        <div className="relative w-10 h-10 shrink-0">
            <Skeleton circle width={40} height={40} />
        </div>

        {/* Content */}
        <div className="flex-1 min-w-0">
            <div className="flex justify-between items-baseline mb-1">
                <div className="w-1/3">
                    <Skeleton width="100%" height={16} />
                </div>
                <div className="w-1/4 flex justify-end">
                    <Skeleton width="60%" height={12} />
                </div>
            </div>
            <div className="mb-1">
                <Skeleton width="90%" height={16} />
            </div>
            <div>
                <Skeleton count={2} height={12} />
            </div>
        </div>
    </div>
);

export default function InboxListSkeleton({ count = 6 }) {
    return (
        <SkeletonTheme baseColor="#f3f4f6" highlightColor="#f9fafb">
            <div className="flex flex-col">
                {Array(count).fill(0).map((_, i) => (
                    <ThreadSkeleton key={i} />
                ))}
            </div>
        </SkeletonTheme>
    );
}
