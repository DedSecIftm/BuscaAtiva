import AppBar from '@mui/material/AppBar'
import Container from '@mui/material/Container'
import Toolbar from '@mui/material/Toolbar'
import Typography from '@mui/material/Typography'
import ImportarFrequencias from './pages/ImportarFrequencias.tsx'

function App() {
  return (
    <>
      <AppBar position="static" elevation={0}>
        <Toolbar>
          <Typography variant="h6" component="span" sx={{ fontWeight: 700 }}>
            Busca Ativa
          </Typography>
        </Toolbar>
      </AppBar>
      <Container maxWidth="md" sx={{ py: 4 }}>
        <ImportarFrequencias />
      </Container>
    </>
  )
}

export default App
