import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import { ErroApi } from '../regras/api.ts'
import { buscarUsuarioAtual } from './api.ts'
import type { UsuarioAtual } from './tipos.ts'

export interface EstadoUsuarioAtual {
  usuario: UsuarioAtual | null
  carregando: boolean
  erro: string | null
  tentarNovamente: () => void
}

// O Layout busca o usuário uma vez e repassa às telas, evitando uma chamada a /me por tela.
export const UsuarioAtualContext = createContext<EstadoUsuarioAtual | null>(null)

export function useUsuarioAtualDoContexto(): EstadoUsuarioAtual {
  const estado = useContext(UsuarioAtualContext)
  if (!estado) throw new Error('useUsuarioAtualDoContexto precisa estar dentro do Layout.')
  return estado
}

// onSessaoExpirada é chamado quando o servidor recusa o token (vencido ou inválido).
export default function useUsuarioAtual(onSessaoExpirada: () => void): EstadoUsuarioAtual {
  const [usuario, setUsuario] = useState<UsuarioAtual | null>(null)
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState<string | null>(null)

  const buscar = useCallback(() => {
    buscarUsuarioAtual()
      .then(setUsuario)
      .catch((e: unknown) => {
        if (e instanceof ErroApi && e.status === 401) {
          onSessaoExpirada()
          return
        }
        setErro(e instanceof Error ? e.message : 'Ocorreu um erro inesperado.')
      })
      .finally(() => setCarregando(false))
  }, [onSessaoExpirada])

  useEffect(buscar, [buscar])

  function tentarNovamente() {
    setCarregando(true)
    setErro(null)
    buscar()
  }

  return { usuario, carregando, erro, tentarNovamente }
}
