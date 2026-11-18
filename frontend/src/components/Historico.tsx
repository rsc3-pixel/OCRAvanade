import { useEffect, useState } from 'react'
import { listarEnvios, type Envio } from '../api'

export function Historico({ aoAbrir }: { aoAbrir: (envio: Envio) => void }) {
  const [envios, setEnvios] = useState<Envio[] | null>(null)
  const [erro, setErro] = useState<string | null>(null)

  useEffect(() => {
    listarEnvios().then(setEnvios).catch((e: Error) => setErro(e.message))
  }, [])

  if (erro) return <p className="mensagem mensagem--erro">{erro}</p>
  if (!envios) return <p className="vazio">Carregando envios...</p>
  if (envios.length === 0) return <p className="vazio">Nenhum documento enviado ainda.</p>

  return (
    <ul className="historico">
      {envios.map((e) => (
        <li key={e.id}>
          <button type="button" className="historico__item" onClick={() => aoAbrir(e)}>
            <span className="historico__arquivo">
              <strong>{e.nomeArquivo}</strong>
              <small>{new Date(e.criadoEm).toLocaleString('pt-BR')}</small>
            </span>
            <span className={`selo selo--${e.status === 'APROVADO' ? 'aprovado' : 'reprovado'}`}>
              {e.status === 'APROVADO' ? 'Aprovado' : 'Reprovado'}
            </span>
          </button>
        </li>
      ))}
    </ul>
  )
}
