import { useState } from 'react'
import AppBar from '@mui/material/AppBar'
import Box from '@mui/material/Box'
import Container from '@mui/material/Container'
import Tab from '@mui/material/Tab'
import Tabs from '@mui/material/Tabs'
import Toolbar from '@mui/material/Toolbar'
import Typography from '@mui/material/Typography'
import ImportarFrequencias from './pages/ImportarFrequencias.tsx'
import RegrasIrregularidade from './pages/RegrasIrregularidade.tsx'
import Login from './components/Login/Login.tsx'

type Pagina = 'importar' | 'regras'

function App() {
  const [pagina, setPagina] = useState<Pagina>('importar');
  const [token, setToken] = useState<string | null>(null);

  if (!token) {
    return <Login setToken={setToken} />
  }

  return (
    <>
      <AppBar position="static" elevation={0}>
        <Toolbar sx={{ flexWrap: 'wrap', columnGap: 3 }}>
          <Typography variant="h6" component="span" sx={{ fontWeight: 700 }}>
            Busca Ativa
          </Typography>
          <Tabs
            value={pagina}
            onChange={(_, valor: Pagina) => setPagina(valor)}
            textColor="inherit"
            indicatorColor="secondary"
            variant="scrollable"
            scrollButtons={false}
          >
            <Tab value="importar" label="Importar frequências" />
            <Tab value="regras" label="Regras de irregularidade" />
          </Tabs>
        </Toolbar>
      </AppBar>
      <Container maxWidth="md" sx={{ py: 4 }}>
        {/* As duas telas ficam montadas para não perder o que já foi carregado ao trocar de aba. */}
        <Box hidden={pagina !== 'importar'}>
          <ImportarFrequencias />
        </Box>
        <Box hidden={pagina !== 'regras'}>
          <RegrasIrregularidade />
        </Box>
      </Container>
    </>
  )
}

export default App
