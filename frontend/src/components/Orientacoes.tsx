import { Icone, type NomeIcone } from './Icone'

const EXEMPLOS: { icone: NomeIcone; titulo: string; texto: string }[] = [
  { icone: 'agua', titulo: 'Conta de água', texto: 'Compesa, Sabesp, Copasa' },
  { icone: 'luz', titulo: 'Conta de luz', texto: 'Neoenergia, Enel, Cemig' },
  { icone: 'telefone', titulo: 'Telefone ou internet', texto: 'Vivo, Claro, TIM' },
  { icone: 'cartao', titulo: 'Fatura de cartão', texto: 'Banco ou operadora' },
]

const DICAS = [
  'Imagem nítida e sem cortes',
  'Documento inteiro e legível',
  'Sem reflexos ou sombras',
  'Texto reto, alinhado à câmera',
  'Arquivo de até 10 MB',
]

export function Orientacoes() {
  return (
    <aside className="orientacoes">
      <section className="cartao">
        <h2 className="cartao__titulo">O que vale como comprovante</h2>
        <ul className="exemplos">
          {EXEMPLOS.map((e) => (
            <li key={e.titulo} className="exemplo">
              <span className="exemplo__icone">
                <Icone nome={e.icone} />
              </span>
              <span>
                <strong>{e.titulo}</strong>
                <small>{e.texto}</small>
              </span>
            </li>
          ))}
        </ul>
      </section>

      <section className="cartao">
        <h2 className="cartao__titulo">Para uma leitura melhor</h2>
        <ul className="dicas">
          {DICAS.map((d) => (
            <li key={d}>
              <Icone nome="check" tamanho={16} />
              {d}
            </li>
          ))}
        </ul>
      </section>

      <section className="aviso">
        <Icone nome="alerta" tamanho={22} />
        <div>
          <strong>Importante</strong>
          <p>Use um comprovante emitido nos últimos 90 dias e no seu nome. Documentos fora dessas regras são reprovados.</p>
        </div>
      </section>
    </aside>
  )
}
