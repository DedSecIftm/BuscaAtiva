import { useState } from 'react'
import type { FormEvent } from 'react'
import Alert from '@mui/material/Alert'
import Button from '@mui/material/Button'
import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogTitle from '@mui/material/DialogTitle'
import FormControl from '@mui/material/FormControl'
import FormControlLabel from '@mui/material/FormControlLabel'
import FormLabel from '@mui/material/FormLabel'
import Radio from '@mui/material/Radio'
import RadioGroup from '@mui/material/RadioGroup'
import Stack from '@mui/material/Stack'
import TextField from '@mui/material/TextField'
import useMediaQuery from '@mui/material/useMediaQuery'
import { useTheme } from '@mui/material/styles'
import { ErroApi } from '../regras/api.ts'
import type { ErrosDosCampos } from '../regras/api.ts'
import { descreverRegra, validarRegra } from '../regras/tipos.ts'
import type { DadosRegra, TipoRegra } from '../regras/tipos.ts'

const REGRA_VAZIA: DadosRegra = { nome: '', tipo: 'seguidas', quantidadeFaltas: 3, periodoEmDias: 10 }

interface Props {
  inicial?: DadosRegra
  onSalvar: (dados: DadosRegra) => Promise<void>
  onCancelar: () => void
}

function FormularioRegra({ inicial, onSalvar, onCancelar }: Props) {
  const telaPequena = useMediaQuery(useTheme().breakpoints.down('sm'))
  const [dados, setDados] = useState<DadosRegra>(inicial ?? REGRA_VAZIA)
  const [tentouSalvar, setTentouSalvar] = useState(false)
  const [salvando, setSalvando] = useState(false)
  const [errosDoServidor, setErrosDoServidor] = useState<ErrosDosCampos>({})
  const [erroGeral, setErroGeral] = useState<string | null>(null)
  const erros = validarRegra(dados)
  const valida = Object.keys(erros).length === 0

  function alterar<Campo extends keyof DadosRegra>(campo: Campo, valor: DadosRegra[Campo]) {
    setDados((atuais) => ({ ...atuais, [campo]: valor }))
    setErrosDoServidor((atuais) => ({ ...atuais, [campo]: undefined }))
  }

  async function aoEnviar(evento: FormEvent) {
    evento.preventDefault()
    setTentouSalvar(true)
    if (!valida || salvando) return

    setSalvando(true)
    setErroGeral(null)
    try {
      await onSalvar({ ...dados, nome: dados.nome.trim() })
    } catch (erro) {
      const erroApi = erro instanceof ErroApi ? erro : new ErroApi('Não foi possível salvar a regra.')
      setErrosDoServidor(erroApi.erros)
      if (Object.keys(erroApi.erros).length === 0) setErroGeral(erroApi.message)
      setSalvando(false)
    }
  }

  const erroVisivel = (campo: keyof DadosRegra) =>
    errosDoServidor[campo] ?? (tentouSalvar ? erros[campo] : undefined)

  return (
    <Dialog open onClose={salvando ? undefined : onCancelar} maxWidth="sm" fullWidth fullScreen={telaPequena}>
      <form onSubmit={aoEnviar} noValidate>
        <DialogTitle>{inicial ? 'Editar regra' : 'Nova regra'}</DialogTitle>
        <DialogContent dividers>
          <Stack spacing={3}>
            <TextField
              label="Nome da regra"
              placeholder="Ex.: Faltas seguidas"
              value={dados.nome}
              onChange={(e) => alterar('nome', e.target.value)}
              error={!!erroVisivel('nome')}
              helperText={erroVisivel('nome')}
              autoFocus
              fullWidth
            />

            <FormControl>
              <FormLabel>Como as faltas devem ser contadas?</FormLabel>
              <RadioGroup value={dados.tipo} onChange={(e) => alterar('tipo', e.target.value as TipoRegra)}>
                <FormControlLabel value="seguidas" control={<Radio />} label="Faltas seguidas (dias em sequência)" />
                <FormControlLabel
                  value="noPeriodo"
                  control={<Radio />}
                  label="Faltas dentro de um período (seguidas ou intercaladas)"
                />
              </RadioGroup>
            </FormControl>

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <TextField
                label="Quantidade de faltas"
                type="number"
                value={Number.isNaN(dados.quantidadeFaltas) ? '' : dados.quantidadeFaltas}
                onChange={(e) => alterar('quantidadeFaltas', parseInt(e.target.value, 10))}
                error={!!erroVisivel('quantidadeFaltas')}
                helperText={erroVisivel('quantidadeFaltas')}
                slotProps={{ htmlInput: { min: 1 } }}
                fullWidth
              />
              {dados.tipo === 'noPeriodo' && (
                <TextField
                  label="Período (em dias)"
                  type="number"
                  value={Number.isNaN(dados.periodoEmDias) ? '' : dados.periodoEmDias}
                  onChange={(e) => alterar('periodoEmDias', parseInt(e.target.value, 10))}
                  error={!!erroVisivel('periodoEmDias')}
                  helperText={erroVisivel('periodoEmDias')}
                  slotProps={{ htmlInput: { min: 1 } }}
                  fullWidth
                />
              )}
            </Stack>

            {erroGeral && <Alert severity="error">{erroGeral}</Alert>}

            {valida && (
              <Alert severity="info" icon={false}>
                <strong>A situação será irregular quando:</strong> {descreverRegra(dados)}
              </Alert>
            )}
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={onCancelar} disabled={salvando}>
            Cancelar
          </Button>
          <Button type="submit" variant="contained" loading={salvando}>
            Salvar regra
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  )
}

export default FormularioRegra
