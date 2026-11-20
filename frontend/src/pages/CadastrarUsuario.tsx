import { useState } from 'react'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import CircularProgress from '@mui/material/CircularProgress'
import Stack from '@mui/material/Stack'
import FormularioUsuario from '../components/FormularioUsuario.tsx'
import { criarUsuario } from '../usuarios/api.ts'
import { ehAdministrador } from '../usuarios/tipos.ts'
import type { DadosUsuario } from '../usuarios/tipos.ts'
import { useUsuarioAtualDoContexto } from '../usuarios/useUsuarioAtual.ts'

function CadastrarUsuario() {
  const { usuario, carregando, erro, tentarNovamente } = useUsuarioAtualDoContexto()
  // Trocar a chave recria o formulário, deixando-o vazio para o próximo cadastro.
  const [cadastros, setCadastros] = useState(0)
  const [ultimoCadastrado, setUltimoCadastrado] = useState<string | null>(null)

  async function salvar(dados: DadosUsuario) {
    await criarUsuario(dados)
    setUltimoCadastrado(dados.nome)
    setCadastros((atual) => atual + 1)
  }

  if (carregando) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}>
        <CircularProgress aria-label="Carregando" />
      </Box>
    )
  }
  if (erro) {
    return (
      <Alert
        severity="error"
        action={
          <Button color="inherit" onClick={tentarNovamente}>
            Tentar novamente
          </Button>
        }
      >
        {erro}
      </Alert>
    )
  }
  if (!ehAdministrador(usuario)) {
    return <Alert severity="warning">Somente administradores podem cadastrar usuários.</Alert>
  }

  return (
    <Stack spacing={3}>
      {ultimoCadastrado && (
        <Alert severity="success" onClose={() => setUltimoCadastrado(null)}>
          Usuário {ultimoCadastrado} cadastrado. O acesso já pode ser feito com o e-mail e a senha informados.
        </Alert>
      )}
      <FormularioUsuario key={cadastros} onSalvar={salvar} />
    </Stack>
  )
}

export default CadastrarUsuario
