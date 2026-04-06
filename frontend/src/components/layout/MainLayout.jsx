import React, { useState } from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import ComposeModal from '../ComposeModal';

export default function MainLayout() {
	const [isComposeOpen, setIsComposeOpen] = useState(false);

	return (
		<div className="flex w-screen h-screen overflow-hidden">
			<Sidebar onCompose={() => setIsComposeOpen(true)} />
			<div className="flex-1 flex overflow-hidden">
				<Outlet />
			</div>
			{isComposeOpen && <ComposeModal onClose={() => setIsComposeOpen(false)} />}
		</div>
	);
}
