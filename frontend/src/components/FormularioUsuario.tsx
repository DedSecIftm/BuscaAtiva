import { useState } from 'react'
import type { FormEvent } from 'react'
import Alert from '@mui/material/Alert'
import Button from '@mui/material/Button'
import MenuItem from '@mui/material/MenuItem'
import Stack from '@mui/material/Stack'
import TextField from '@mui/material/TextField'
import Typography from '@mui/material/Typography'
import { ErroApi } from '../regras/api.ts'
import type { ErrosDosCampos } from '../regras/api.ts'
import { validarUsuario } from '../usuarios/tipos.ts'
import type { DadosFormularioUsuario, DadosUsuario, PapelUsuario } from '../usuarios/tipos.ts'

const USUARIO_VAZIO: DadosFormularioUsuario = { nome: '', email: '', password: '', confirmacaoSenha: '', role: 'ROLE_USER' }

interface Props {
  onSalvar: (dados: DadosUsuario) => Promise<void>
}

function FormularioUsuario({ onSalvar }: Props) {
  const [dados, setDados] = useState<DadosFormularioUsuario>(USUARIO_VAZIO)
  const [tentouSalvar, setTentouSalvar] = useState(false)
  const [salvando, setSalvando] = useState(false)
  const [errosDoServidor, setErrosDoServidor] = useState<ErrosDosCampos<DadosFormularioUsuario>>({})
  const [erroGeral, setErroGeral] = useState<string | null>(null)
  const erros = validarUsuario(dados)
  const valida = Object.keys(erros).length === 0

  function alterar<Campo extends keyof DadosFormularioUsuario>(campo: Campo, valor: DadosFormularioUsuario[Campo]) {
    setDados((atuais) => ({ ...atuais, [campo]: valor }))
    setErroGeral(null)
    setErrosDoServidor((atuais) => ({ ...atuais, [campo]: undefined }))
  }

  async function aoEnviar(evento: FormEvent) {
    evento.preventDefault()
    setTentouSalvar(true)
    if (!valida || salvando) return

    setSalvando(true)
    setErroGeral(null)
    try {
      await onSalvar({ nome: dados.nome.trim(), email: dados.email.trim(), password: dados.password, role: dados.role })
    } catch (erro) {
      const erroApi = erro instanceof ErroApi ? erro : new ErroApi<DadosUsuario>('Não foi possível salvar o usuário.')
      setErrosDoServidor(erroApi.erros)
      if (Object.keys(erroApi.erros).length === 0) setErroGeral(erroApi.message)
      setSalvando(false)
    }
  }

  const erroVisivel = (campo: keyof DadosFormularioUsuario) =>
    errosDoServidor[campo] ?? (tentouSalvar ? erros[campo] : undefined)

  return (
    <form onSubmit={aoEnviar} noValidate>
      <Stack spacing={3}>
        <Typography variant="h5" component="h1">
          Novo usuário
        </Typography>
        {erroGeral && <Alert severity="error">{erroGeral}</Alert>}
        <TextField
          label="Nome"
          placeholder="Nome do usuário"
          value={dados.nome}
          onChange={(e) => alterar('nome', e.target.value)}
          error={!!erroVisivel('nome')}
          helperText={erroVisivel('nome')}
          autoFocus
          fullWidth
        />
        <TextField
          label="E-mail"
          type="email"
          placeholder="nome@escola.gov.br"
          value={dados.email}
          onChange={(e) => alterar('email', e.target.value)}
          error={!!erroVisivel('email')}
          helperText={erroVisivel('email')}
          fullWidth
        />
        <TextField
          label="Senha"
          type="password"
          autoComplete="new-password"
          value={dados.password}
          onChange={(e) => alterar('password', e.target.value)}
          error={!!erroVisivel('password')}
          helperText={erroVisivel('password') ?? 'Use pelo menos 8 caracteres.'}
          fullWidth
        />
        <TextField
          label="Confirmar senha"
          type="password"
          autoComplete="new-password"
          value={dados.confirmacaoSenha}
          onChange={(e) => alterar('confirmacaoSenha', e.target.value)}
          error={!!erroVisivel('confirmacaoSenha')}
          helperText={erroVisivel('confirmacaoSenha')}
          fullWidth
        />
        <TextField
          select
          label="Tipo de usuário"
          value={dados.role}
          onChange={(e) => alterar('role', e.target.value as PapelUsuario)}
          error={!!erroVisivel('role')}
          helperText={erroVisivel('role') ?? 'Administradores também podem cadastrar outros usuários.'}
          fullWidth
        >
          <MenuItem value="ROLE_USER">Usuário</MenuItem>
          <MenuItem value="ROLE_ADMIN">Administrador</MenuItem>
        </TextField>
        <Button type="submit" variant="contained" disabled={salvando}>
          {salvando ? 'Salvando...' : 'Salvar'}
        </Button>
      </Stack>
    </form>
  )
}

export default FormularioUsuario
