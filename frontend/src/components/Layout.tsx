import AppBar from '@mui/material/AppBar'
import Button from '@mui/material/Button'
import Container from '@mui/material/Container'
import Tab from '@mui/material/Tab'
import Tabs from '@mui/material/Tabs'
import Toolbar from '@mui/material/Toolbar'
import Typography from '@mui/material/Typography'
import LogoutOutlinedIcon from '@mui/icons-material/LogoutOutlined'
import { Link, Outlet, useLocation } from 'react-router'
import useArquivosImportados from '../importacao/useArquivosImportados.ts'
import { ehAdministrador } from '../usuarios/tipos.ts'
import useUsuarioAtual, { UsuarioAtualContext } from '../usuarios/useUsuarioAtual.ts'

function abaDoCaminho(pathname: string, administrador: boolean): string | false {
  if (pathname.startsWith('/regras')) return '/regras'
  // Sem a aba de usuários no menu, nenhuma aba fica selecionada nessa tela.
  if (pathname.startsWith('/usuarios')) return administrador ? '/usuarios' : false
  return '/importar'
}

interface Props {
  onSair: () => void
}

function Layout({ onSair }: Props) {
  const arquivos = useArquivosImportados()
  const { pathname } = useLocation()
  const estadoDoUsuario = useUsuarioAtual(onSair)
  const administrador = ehAdministrador(estadoDoUsuario.usuario)
  const abaAtual = abaDoCaminho(pathname, administrador)

  return (
    <>
      <AppBar position="static" elevation={0}>
        <Toolbar sx={{ flexWrap: 'wrap', columnGap: 3 }}>
          <Typography variant="h6" component="span" sx={{ fontWeight: 700 }}>
            Busca Ativa
          </Typography>
          {/* No celular as abas descem para uma linha própria, deixando "Sair" ao lado do título. */}
          <Tabs
            value={abaAtual}
            textColor="inherit"
            indicatorColor="secondary"
            variant="scrollable"
            scrollButtons={false}
            sx={{ order: { xs: 1, sm: 0 }, width: { xs: '100%', sm: 'auto' } }}
          >
            <Tab value="/importar" label="Importar frequências" component={Link} to="/importar" />
            <Tab value="/regras" label="Regras de irregularidade" component={Link} to="/regras" />
            {administrador && <Tab value="/usuarios" label="Usuários" component={Link} to="/usuarios/novo" />}
          </Tabs>
          <Button color="inherit" startIcon={<LogoutOutlinedIcon />} onClick={onSair} sx={{ ml: 'auto' }}>
            Sair
          </Button>
        </Toolbar>
      </AppBar>
      <Container maxWidth="md" sx={{ py: 4 }}>
        <UsuarioAtualContext value={estadoDoUsuario}>
          <Outlet context={arquivos} />
        </UsuarioAtualContext>
      </Container>
    </>
  )
}

export default Layout
