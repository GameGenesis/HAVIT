import React from 'react'

import Header from './Header';

interface LayoutProps {
    Component: React.FunctionComponent
}

const Layout: React.FunctionComponent<LayoutProps> = ({ Component }) => {
    return (
        <>
            <Header />
            
            <div className="mx-auto max-w-screen-xl py-2 px-4 lg:px-8 lg:py-4">
                <Component />
            </div>
        </>
    );
}

export default Layout;