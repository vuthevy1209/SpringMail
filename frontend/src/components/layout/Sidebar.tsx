import React, { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import {
    Inbox,
    Send,
    FileText,
    Trash2,
    Settings,
    LogOut,
    X,
    ChevronLeft,
    ChevronRight,
    Star,
    Bookmark,
    AlertOctagon,
    PenSquare,
    Edit,
    Mail,
    Calendar,
} from "lucide-react";
import { useAuth } from "../../context/AuthContext";
import SpringIcon from "../../assets/spring-icon.svg";
import ComposeModal from "../emails/ComposeModal";

import { LAYOUT } from "../../constants/layout";

const iconMap = {
    Inbox: Inbox,
    Send: Send,
    FileText: FileText,
    Trash2: Trash2,
    Star: Star,
    Bookmark: Bookmark,
    AlertOctagon: AlertOctagon,
    Mail: Mail,
};

export const folders = [
    { id: "inbox", name: "Inbox", icon: "Inbox", count: 12 },
    { id: "sent", name: "Sent", icon: "Send", count: 0 },
    { id: "drafts", name: "Drafts", icon: "FileText", count: 3 },
    { id: "starred", name: "Starred", icon: "Star", count: 0 },
    { id: "important", name: "Important", icon: "Bookmark", count: 0 },
    { id: "spam", name: "Spam", icon: "AlertOctagon", count: 0 },
    { id: "all", name: "All Mail", icon: "Mail", count: 0 },

    { id: "trash", name: "Trash", icon: "Trash2", count: 0 },
];

export default function Sidebar() {
    const location = useLocation();
    const navigate = useNavigate();
    const { user, logout } = useAuth(); // Global user state from context
    const [isSettingsOpen, setIsSettingsOpen] = useState(false);
    const [isCollapsed, setIsCollapsed] = useState(false);
    const [isComposeOpen, setIsComposeOpen] = useState(false);

    const handleLogout = async () => {
        try {
            await logout();
            setIsSettingsOpen(false);
        } catch (error) {
            console.error("Logout failed", error);
        }
    };

    // Active folder = the first path segment, e.g. "/inbox" → "inbox"
    const activeFolder =
        location.pathname.replace("/", "").split("/")[0] || "inbox";

    return (
        <div
            className={`relative bg-canvas-gray ${LAYOUT.COLUMN_BORDER} flex flex-col h-screen py-6 transition-all duration-300 ease-in-out ${isCollapsed ? LAYOUT.SIDEBAR_COLLAPSED_WIDTH + " px-2.5" : LAYOUT.SIDEBAR_WIDTH + " px-5"}`}
        >
            {/* Collapse Toggle Button */}
            <button
                onClick={() => setIsCollapsed(!isCollapsed)}
                className="absolute -right-4 top-1/2 -translate-y-1/2 bg-pure-surface border border-whisper rounded-full w-8 h-8 flex items-center justify-center shadow-md hover:bg-canvas-gray text-muted-steel hover:text-charcoal-ink transition-colors z-30 cursor-pointer active:scale-90"
                aria-label={isCollapsed ? "Expand sidebar" : "Collapse sidebar"}
            >
                {isCollapsed ? <ChevronRight size={18} /> : <ChevronLeft size={18} />}
            </button>

            {/* Brand */}
            <div
                className={`flex items-center mb-8 overflow-hidden transition-all duration-300 ${isCollapsed ? "justify-center px-0" : "gap-4 px-2"}`}
            >
                <div className="flex items-center shrink-0">
                    <img
                        src={SpringIcon}
                        alt="SpringMail Logo"
                        className="w-[32px] h-[32px]"
                    />
                </div>
                {!isCollapsed && (
                    <span className="font-bold tracking-tight text-xl transition-all duration-300 whitespace-nowrap overflow-hidden text-charcoal-ink">
                        SpringMail
                    </span>
                )}
            </div>

            {/* Compose Button */}
            <div
                className={`mb-6 flex ${isCollapsed ? "justify-center px-0" : "px-2"}`}
            >
                <button
                    onClick={() => setIsComposeOpen(true)}
                    className={`flex items-center bg-spring-green text-white hover:bg-spring-green/90 transition-all shadow-sm ${isCollapsed
                            ? "h-12 w-12 rounded-xl justify-center p-0"
                            : "h-12 w-full rounded-2xl justify-center gap-3 px-4"
                        }`}
                >
                    <Edit size={18} className="shrink-0" />
                    {!isCollapsed && (
                        <span className="font-semibold text-[15px]">Compose</span>
                    )}
                </button>
            </div>

            {/* Folders */}
            <div className="flex flex-col gap-1 flex-1">
                {folders.map((folder) => {
                    const Icon = iconMap[folder.icon];
                    const isActive = activeFolder === folder.id;

                    return (
                        <button
                            key={folder.id}
                            onClick={() => navigate(`/${folder.id}`)}
                            className={`flex items-center ${isCollapsed ? "justify-center p-0 h-12 w-12 mx-auto" : "justify-between px-4 py-3 w-full"} rounded-xl border transition-all ${isActive
                                    ? "bg-pure-surface text-charcoal-ink font-bold shadow-sm border-whisper/50"
                                    : "bg-transparent text-muted-steel font-medium border-transparent hover:bg-white/60"
                                }`}
                        >
                            <div
                                className={`flex items-center ${isCollapsed ? "justify-center" : "gap-4 overflow-hidden"}`}
                            >
                                <div className="relative">
                                    <Icon
                                        size={22}
                                        className={`shrink-0 ${isActive ? "text-spring-green" : ""}`}
                                    />
                                    {isCollapsed && folder.count > 0 && (
                                        <span className="absolute -top-2 -right-2 bg-spring-green text-white text-[10px] font-bold h-5 w-5 flex items-center justify-center rounded-full border-2 border-canvas-gray shadow-sm">
                                            {folder.count > 99 ? "99+" : folder.count}
                                        </span>
                                    )}
                                </div>
                                {!isCollapsed && (
                                    <span className="text-[15px] transition-all duration-300 whitespace-nowrap overflow-hidden">
                                        {folder.name}
                                    </span>
                                )}
                            </div>
                            {!isCollapsed && folder.count > 0 && (
                                <span
                                    className={`text-xs px-2.5 py-1 rounded-xl font-bold ${isActive
                                            ? "bg-spring-green text-white shadow-sm"
                                            : "bg-whisper/40 text-muted-steel"
                                        }`}
                                >
                                    {folder.count}
                                </span>
                            )}
                        </button>
                    );
                })}

                <div className="my-2 mx-4 border-t border-whisper/80" />

                <button
                    onClick={() => navigate('/events')}
                    className={`flex items-center ${isCollapsed ? "justify-center p-0 h-12 w-12 mx-auto" : "justify-between px-4 py-3 w-full"} rounded-xl border transition-all ${
                            location.pathname.startsWith('/events')
                            ? "bg-pure-surface text-charcoal-ink font-bold shadow-sm border-whisper/50"
                            : "bg-transparent text-muted-steel font-medium border-transparent hover:bg-white/60"
                        }`}
                >
                    <div
                        className={`flex items-center ${isCollapsed ? "justify-center" : "gap-4 overflow-hidden"}`}
                    >
                        <div className="relative">
                            <Calendar
                                size={22}
                                className={`shrink-0 ${location.pathname.startsWith('/events') ? "text-spring-green" : ""}`}
                            />
                        </div>
                        {!isCollapsed && (
                            <span className="text-[15px] transition-all duration-300 whitespace-nowrap overflow-hidden">
                                Events
                            </span>
                        )}
                    </div>
                </button>
            </div>

            {/* User Profile */}
            {user && (
                <div
                    className={`mt-auto mb-2 pt-5 border-t border-whisper/50 flex items-center ${isCollapsed ? "justify-center" : "gap-4 px-1"} overflow-hidden transition-all duration-300`}
                >
                    <img
                        src={user.avatar}
                        alt="User avatar"
                        onClick={isCollapsed ? () => setIsSettingsOpen(true) : undefined}
                        className={`w-11 h-11 rounded-full object-cover border border-whisper shrink-0 transition-transform shadow-sm ${isCollapsed ? "cursor-pointer hover:scale-110 active:scale-95" : ""}`}
                        referrerPolicy="no-referrer"
                    />
                    <div
                        className={`flex flex-col overflow-hidden transition-all duration-300 ${isCollapsed ? "w-0 opacity-0" : "w-full opacity-100"}`}
                    >
                        <span className="text-[15px] font-bold text-charcoal-ink truncate leading-tight">
                            {user.name || user.givenName}
                        </span>
                        <span className="text-xs text-muted-steel truncate">
                            {user.email}
                        </span>
                    </div>

                    {/* Settings */}
                    {!isCollapsed && (
                        <div>
                            <button
                                onClick={() => setIsSettingsOpen(true)}
                                className="flex items-center gap-3 p-1.5 text-muted-steel rounded-lg hover:bg-white/80 transition-colors"
                            >
                                <Settings size={22} />
                            </button>
                        </div>
                    )}
                </div>
            )}

            {/* Settings Modal Overlay */}
            {isSettingsOpen && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/20 backdrop-blur-[2px]">
                    <div className="bg-pure-surface border border-whisper w-[320px] rounded-2xl shadow-xl flex flex-col overflow-hidden">
                        {/* Header */}
                        <div className="flex justify-between items-center p-4 border-b border-whisper/50 bg-canvas-gray/50">
                            <h3 className="font-semibold text-charcoal-ink text-[15px]">
                                Profile & Settings
                            </h3>
                            <button
                                onClick={() => setIsSettingsOpen(false)}
                                className="text-muted-steel hover:text-charcoal-ink transition-colors p-1 rounded-md hover:bg-white/80 shrink-0"
                            >
                                <X size={18} />
                            </button>
                        </div>

                        {/* Body - User Info */}
                        {user && (
                            <div className="p-6 flex flex-col items-center text-center gap-3">
                                <img
                                    src={user.avatar}
                                    alt="Avatar"
                                    className="w-16 h-16 rounded-full border border-whisper object-cover shadow-sm"
                                    referrerPolicy="no-referrer"
                                />
                                <div>
                                    <h4 className="font-bold text-charcoal-ink">
                                        {user.name || user.givenName}
                                    </h4>
                                    <p className="text-sm text-muted-steel">{user.email}</p>
                                </div>
                            </div>
                        )}

                        {/* Actions */}
                        <div className="p-4 pt-2 pb-5">
                            <button
                                onClick={handleLogout}
                                className="w-full flex items-center justify-center gap-2 bg-red-50 text-red-600 font-semibold py-2.5 rounded-lg border border-red-100 hover:bg-red-100 active:bg-red-200 transition-colors"
                            >
                                <LogOut size={16} />
                                Sign Out
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Compose Email Modal */}
            <ComposeModal
                isOpen={isComposeOpen}
                onClose={() => setIsComposeOpen(false)}
            />
        </div>
    );
}
