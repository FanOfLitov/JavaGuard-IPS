import {
    Route,
    Routes
} from 'react-router'

import AppLayout from './components/AppLayout.jsx'
import DashboardPage from './pages/DashboardPage.jsx'
import AlertsPage from './pages/AlertsPage.jsx'


export default function App() {

    return (
        <Routes>

            <Route element={<AppLayout />}>

                <Route
                    index
                    element={<DashboardPage />}
                />

                <Route
                    path="alerts"
                    element={<AlertsPage />}
                />

            </Route>

        </Routes>
    )
}