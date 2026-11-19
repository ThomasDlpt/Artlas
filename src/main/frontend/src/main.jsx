import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import 'leaflet/dist/leaflet.css'
import './styles/artlas-tokens.css'
import './styles/global.css'
import App from './App.jsx'
import { NotificationProvider } from './context/NotificationContext.jsx'
import { CircuitProvider } from './context/CircuitContext.jsx'
import { AdminAuthProvider } from './context/AdminAuthContext.jsx'

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <NotificationProvider>
        <CircuitProvider>
          <AdminAuthProvider>
            <App />
          </AdminAuthProvider>
        </CircuitProvider>
      </NotificationProvider>
    </BrowserRouter>
  </React.StrictMode>,
)
