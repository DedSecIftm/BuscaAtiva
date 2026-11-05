import { useState } from 'react'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Snackbar from '@mui/material/Snackbar'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import { useOutletContext } from 'react-router'
import AreaDeEnvio from '../components/AreaDeEnvio.tsx'
import ListaDeArquivos from '../components/ListaDeArquivos.tsx'
import PreviaDoArquivo from '../components/PreviaDoArquivo.tsx'
import { tipoDoArquivo } from '../importacao/lerArquivo.ts'
import type { ArquivoCarregado } from '../importacao/tipos.ts'
import type { ArquivosImportados } from '../importacao/useArquivosImportados.ts'

function ImportarFrequencias() {
  const { itens, adicionarArquivos, remover, limpar } = useOutletContext<ArquivosImportados>()
  const [recusados, setRecusados] = useState<string[]>([])
  const [emPrevia, setEmPrevia] = useState<ArquivoCarregado | null>(null)
  const [avisoAberto, setAvisoAberto] = useState(false)

  function receberArquivos(lista: FileList) {
    const arquivos = Array.from(lista)
    setRecusados(arquivos.filter((a) => !tipoDoArquivo(a)).map((a) => a.name))
    adicionarArquivos(arquivos)
  }

  const prontos = itens.filter((item) => item.status === 'pronto')
  const totalRegistros = prontos.reduce((soma, item) => soma + item.tabela.linhas.length, 0)
  const lendo = itens.some((item) => item.status === 'lendo')

  return (
    <Stack spacing={3}>
      <Box>
        <Typography variant="h4" component="h1" gutterBottom sx={{ fontWeight: 700 }}>
          Importar frequências dos alunos
        </Typography>
        <Typography color="text.secondary">
          Envie o arquivo exportado do sistema da escola. Formatos aceitos: Excel, XML ou JSON.
        </Typography>
      </Box>

      <AreaDeEnvio onArquivos={receberArquivos} />

      {recusados.length > 0 && (
        <Alert severity="warning" onClose={() => setRecusados([])}>
          {recusados.length === 1
            ? `O arquivo "${recusados[0]}" não é aceito.`
            : `Os arquivos ${recusados.map((n) => `"${n}"`).join(', ')} não são aceitos.`}{' '}
          Use Excel, XML ou JSON.
        </Alert>
      )}

      <ListaDeArquivos itens={itens} onRemover={remover} onVerDados={setEmPrevia} />

      <Stack
        direction={{ xs: 'column', sm: 'row' }}
        spacing={2}
        sx={{ alignItems: { sm: 'center' }, justifyContent: 'space-between' }}
      >
        <Typography color="text.secondary">
          {prontos.length === 0
            ? 'Nenhum arquivo pronto para importar'
            : `${prontos.length} ${prontos.length === 1 ? 'arquivo pronto' : 'arquivos prontos'} · ${totalRegistros} ${totalRegistros === 1 ? 'registro' : 'registros'}`}
        </Typography>
        <Stack direction="row" spacing={2}>
          <Button variant="outlined" disabled={itens.length === 0} onClick={limpar}>
            Limpar tudo
          </Button>
          <Button
            variant="contained"
            size="large"
            disabled={prontos.length === 0 || lendo}
            onClick={() => setAvisoAberto(true)}
          >
            Importar
          </Button>
        </Stack>
      </Stack>

      {emPrevia?.status === 'pronto' && (
        <PreviaDoArquivo
          nome={emPrevia.arquivo.name}
          tabela={emPrevia.tabela}
          onFechar={() => setEmPrevia(null)}
        />
      )}

      <Snackbar
        open={avisoAberto}
        autoHideDuration={4000}
        onClose={() => setAvisoAberto(false)}
        message="Importação será implementada em breve."
      />
    </Stack>
  )
}

export default ImportarFrequencias
