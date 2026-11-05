import AppBar from '@mui/material/AppBar'
import Container from '@mui/material/Container'
import Tab from '@mui/material/Tab'
import Tabs from '@mui/material/Tabs'
import Toolbar from '@mui/material/Toolbar'
import Typography from '@mui/material/Typography'
import { Link, Outlet, useLocation } from 'react-router'
import useArquivosImportados from '../importacao/useArquivosImportados.ts'

function Layout() {
  const arquivos = useArquivosImportados()
  const { pathname } = useLocation()
  const abaAtual = pathname.startsWith('/regras') ? '/regras' : '/importar'

  return (
    <>
      <AppBar position="static" elevation={0}>
        <Toolbar sx={{ flexWrap: 'wrap', columnGap: 3 }}>
          <Typography variant="h6" component="span" sx={{ fontWeight: 700 }}>
            Busca Ativa
          </Typography>
          <Tabs
            value={abaAtual}
            textColor="inherit"
            indicatorColor="secondary"
            variant="scrollable"
            scrollButtons={false}
          >
            <Tab value="/importar" label="Importar frequências" component={Link} to="/importar" />
            <Tab value="/regras" label="Regras de irregularidade" component={Link} to="/regras" />
          </Tabs>
        </Toolbar>
      </AppBar>
      <Container maxWidth="md" sx={{ py: 4 }}>
        <Outlet context={arquivos} />
      </Container>
    </>
  )
}

export default Layout
