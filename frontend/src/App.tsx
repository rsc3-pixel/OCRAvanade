import { useState } from 'react'
import { enviarDocumento, type Envio } from './api'
import { AreaUpload } from './components/AreaUpload'
import { Etapas } from './components/Etapas'
import { Historico } from './components/Historico'
import { Icone } from './components/Icone'
import { Logo } from './components/Logo'
import { Orientacoes } from './components/Orientacoes'
import { Resultado } from './components/Resultado'

type Tela = 'enviar' | 'historico'

// Índices das etapas do componente Etapas
const UPLOAD = 0
const PROCESSAMENTO = 1
const RESULTADO = 3

export default function App() {
  const [tela, setTela] = useState<Tela>('enviar')
  const [arquivo, setArquivo] = useState<File | null>(null)
  const [tipo, setTipo] = useState('COMPROVANTE_RESIDENCIA')
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)
  const [envio, setEnvio] = useState<Envio | null>(null)

  const etapaAtual = envio ? RESULTADO : enviando ? PROCESSAMENTO : UPLOAD

  async function processar() {
    if (!arquivo) return
    setEnviando(true)
    setErro(null)
    try {
      setEnvio(await enviarDocumento(arquivo, tipo))
    } catch (e) {
      setErro((e as Error).message)
    } finally {
      setEnviando(false)
    }
  }

  function recomecar() {
    setArquivo(null)
    setEnvio(null)
    setErro(null)
    setTela('enviar')
  }

  return (
    <div className="app">
      <header className="topo">
        <Logo />
        <p className="topo__contexto">
          <strong>Mentoria 2026.2</strong>
          <span>Recife · Residência Tecnológica</span>
        </p>
      </header>

      <nav className="menu" aria-label="Seções">
        <button type="button" className={`menu__item${tela === 'enviar' ? ' menu__item--ativo' : ''}`} onClick={recomecar}>
          <Icone nome="enviar" />
          Enviar
        </button>
        <button
          type="button"
          className={`menu__item${tela === 'historico' ? ' menu__item--ativo' : ''}`}
          onClick={() => setTela('historico')}
        >
          <Icone nome="lista" />
          Meus envios
        </button>
      </nav>

      <main className="conteudo">
        {tela === 'historico' ? (
          <>
            <h1 className="titulo">Meus envios</h1>
            <p className="subtitulo">Os últimos documentos processados e o resultado de cada um.</p>
            <section className="cartao">
              <Historico
                aoAbrir={(e) => {
                  setEnvio(e)
                  setTela('enviar')
                }}
              />
            </section>
          </>
        ) : (
          <>
            <h1 className="titulo">Enviar comprovante</h1>
            <p className="subtitulo">Envie o documento e a gente lê, confere e responde em segundos.</p>
            <Etapas atual={etapaAtual} />

            <div className="grade">
              <section className="cartao cartao--principal">
                {envio ? (
                  <Resultado envio={envio} aoNovoEnvio={recomecar} />
                ) : (
                  <>
                    <h2 className="cartao__titulo">Selecione o documento</h2>
                    <label className="campo">
                      <span>Tipo de documento</span>
                      <select value={tipo} onChange={(e) => setTipo(e.target.value)} disabled={enviando}>
                        <option value="COMPROVANTE_RESIDENCIA">Comprovante de residência</option>
                      </select>
                    </label>

                    <AreaUpload
                      arquivo={arquivo}
                      desabilitado={enviando}
                      aoEscolher={(a) => {
                        setArquivo(a)
                        setErro(null)
                      }}
                      aoErro={setErro}
                    />

                    {erro && (
                      <p className="mensagem mensagem--erro" role="alert">
                        {erro}
                      </p>
                    )}

                    <div className="acoes">
                      <button type="button" className="botao botao--secundario" onClick={recomecar} disabled={enviando}>
                        Cancelar
                      </button>
                      <button
                        type="button"
                        className="botao botao--primario"
                        onClick={processar}
                        disabled={!arquivo || enviando}
                      >
                        {enviando ? 'Processando...' : 'Processar documento'}
                        {!enviando && <Icone nome="seta" tamanho={18} />}
                      </button>
                    </div>
                  </>
                )}
              </section>
              <Orientacoes />
            </div>
          </>
        )}
      </main>
    </div>
  )
}
