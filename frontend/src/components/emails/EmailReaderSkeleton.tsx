import React from 'react';
import Skeleton, { SkeletonTheme } from 'react-loading-skeleton';
import 'react-loading-skeleton/dist/skeleton.css';

export default function EmailReaderSkeleton() {
    return (
        <SkeletonTheme baseColor="#f3f4f6" highlightColor="#f9fafb">
            <div className="flex-1 flex flex-col h-screen bg-canvas-gray overflow-hidden">
                {/* LLM Actions Toolbar Skeleton */}
                <div className="px-6 py-4 border-b border-whisper/50 flex gap-3 bg-pure-surface">
                    <Skeleton width={140} height={36} borderRadius={8} />
                    <Skeleton width={160} height={36} borderRadius={8} />
                </div>

                {/* Reading Pane Skeleton */}
                <div className="flex-1 px-12 py-8 overflow-y-auto">
                    <div className="max-w-[800px] mx-auto">
                        {/* Title/Subject */}
                        <div className="mb-10 w-3/4">
                            <Skeleton height={32} />
                        </div>

                        {/* Large Email Body Container */}
                        <div className="bg-pure-surface rounded-xl p-8 border border-whisper shadow-sm">
                            {/* Sender Info Area */}
                            <div className="flex justify-between items-center mb-8 pb-6 border-b border-whisper/30">
                                <div className="flex items-center gap-4">
                                    <Skeleton circle width={48} height={48} />
                                    <div>
                                        <div className="w-40 mb-2">
                                            <Skeleton height={18} />
                                        </div>
                                        <div className="w-56">
                                            <Skeleton height={14} />
                                        </div>
                                    </div>
                                </div>
                                <div className="w-28 text-right">
                                    <Skeleton height={14} />
                                </div>
                            </div>

                            {/* Main Reading Content - One Big Block */}
                            <div className="space-y-4">
                                <Skeleton count={1} width="40%" height={24} className="mb-4" />
                                <div className="space-y-3">
                                    <Skeleton count={8} height={16} />
                                    <Skeleton width="85%" height={16} />
                                    <Skeleton width="70%" height={16} />
                                </div>
                                
                                <div className="pt-8 space-y-3">
                                    <Skeleton count={5} height={16} />
                                    <Skeleton width="60%" height={16} />
                                </div>

                                {/* Signature or Footer Area */}
                                <div className="pt-10 w-48">
                                    <Skeleton count={3} height={12} />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </SkeletonTheme>
    );
}
