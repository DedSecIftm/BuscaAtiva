import { Navigate, Route, Routes } from 'react-router'
import Layout from './components/Layout.tsx'
import Login from './components/Login/Login.tsx'
import ProtectedRoute from './components/ProtectedRoute/ProtectedRoute.tsx'
import CadastrarUsuario from './pages/CadastrarUsuario.tsx'
import ImportarFrequencias from './pages/ImportarFrequencias.tsx'
import RegrasIrregularidade from './pages/RegrasIrregularidade.tsx'
import useToken from './regras/useToken.ts'

function App() {
  const { token, expirou, setToken, removeToken } = useToken()

  return (
    <Routes>
      <Route
        path="/login"
        element={token ? <Navigate to="/" replace /> : <Login setToken={setToken} sessaoExpirada={expirou} />}
      />
      <Route
        element={
          <ProtectedRoute token={token}>
            <Layout onSair={removeToken} />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/importar" replace />} />
        <Route path="importar" element={<ImportarFrequencias />} />
        <Route path="regras" element={<RegrasIrregularidade />} />
        <Route path="usuarios/novo" element={<CadastrarUsuario />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default App
