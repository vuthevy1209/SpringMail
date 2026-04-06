import React from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';

export default function MainLayout() {
	return (
		<div className="flex w-screen h-screen overflow-hidden">
			<Sidebar />
			<div className="flex-1 flex overflow-hidden">
				<Outlet />
			</div>
		</div>
	);
}
