import {
    NavLink,
    Outlet
} from 'react-router'


export default function AppLayout() {

    return (
        <>

            <header className="header">

                <div>
                    <h1>JavaGuard IPS</h1>

                    <p>
                        Network Security Platform
                    </p>
                </div>


                <nav className="main-nav">

                    <NavLink
                        to="/"
                        end
                        className={({ isActive }) =>
                            isActive
                                ? 'nav-link active'
                                : 'nav-link'
                        }
                    >
                        Dashboard
                    </NavLink>


                    <NavLink
                        to="/alerts"
                        className={({ isActive }) =>
                            isActive
                                ? 'nav-link active'
                                : 'nav-link'
                        }
                    >
                        Alerts
                    </NavLink>

                </nav>

            </header>


            <main className="container">
                <Outlet />
            </main>

        </>
    )
}