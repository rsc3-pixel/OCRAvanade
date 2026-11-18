# Atesta · validação de comprovantes com OCR

Projeto **OCRAvanade** da Mentoria 2026.2 (Recife, Residência Tecnológica).
O usuário envia um comprovante de residência (PDF, PNG ou JPG) e o sistema lê o documento, extrai os dados e
responde **Aprovado** ou **Reprovado**, mostrando o motivo de cada regra.

> A definição do problema (usuário, documento, dados, regras e fluxo) está em [docs/ENCONTRO2.md](docs/ENCONTRO2.md).

## Como funciona

```
Documento -> OpenCV -> Tesseract -> Regex ou IA -> Regras de negócio -> Postgres -> Resultado na tela
```

| Etapa | O que faz | Onde está |
|---|---|---|
| Conversão | PDF vira imagem a 300 DPI (PDFBox) | `backend/.../imagem/ConversorDocumento.java` |
| OpenCV | Tons de cinza, remove ruído, amplia, preto e branco, corrige rotação | `backend/.../imagem/PreProcessadorImagem.java` |
| Tesseract | Lê o texto em português | `backend/.../ocr/LeitorOcr.java` |
| Extração | Texto vira campos (nome, CPF, endereço, cidade, UF, emissão) | `backend/.../extracao/` |
| Validação | Legível, nome, CPF, endereço, emissão em até 90 dias | `backend/.../validacao/ValidadorDocumento.java` |
| API + banco | `POST /api/envios`, `GET /api/envios`, `GET /api/envios/{id}` | `backend/.../envio/` |
| Tela | Envio com etapas, resultado e histórico | `frontend/src/` |

A extração tem duas implementações atrás da mesma interface (`ExtratorDados`):

- **Regex** (padrão): sem custo, sem chave, ótima para layouts previsíveis.
- **Claude** (opcional): usa *structured outputs* para devolver um JSON garantido no formato certo. Se a IA falhar,
  o sistema cai automaticamente no Regex.

## Rodando

Pré-requisito: Docker.

```bash
docker compose up --build
```

- Tela: http://localhost:3000
- API: http://localhost:8080/api/envios

Para testar, use os arquivos de [amostras/](amostras/) (dados fictícios). O `comprovante-aprovado` foi emitido em
10/11/2026, então passa a ser reprovado depois de 90 dias. Isso é a regra funcionando, não um bug.

### Ligando a IA (opcional)

```bash
cp .env.example .env
# edite .env: OCR_EXTRATOR=CLAUDE e ANTHROPIC_API_KEY=sua-chave
docker compose up --build
```

O modelo padrão é `claude-opus-5-5`. Cada documento é uma chamada paga à API; para volume alto, troque
`OCR_MODELO_IA` por um modelo mais barato e compare a qualidade nas amostras antes.

### Desenvolvimento sem Docker

```bash
# Backend (Java 21). Precisa de um Postgres local e do Tesseract instalado (TESSDATA_PREFIX apontando para o tessdata).
cd backend && ./mvnw spring-boot:run

# Frontend (Node 24). O Vite repassa /api para localhost:8080.
cd frontend && npm install && npm run dev
```

Testes do backend: `cd backend && ./mvnw test`.

## CI/CD

[.github/workflows/ci.yml](.github/workflows/ci.yml):

- **Todo push e pull request:** compila e testa o backend (JUnit), roda lint e build do frontend.
- **Push na `main` com tudo verde:** publica as imagens `backend` e `frontend` no GitHub Container Registry
  (`ghcr.io/rsc3-pixel/ocravanade-backend` e `-frontend`).

## Decisões técnicas

- **Debian 13 na imagem do backend, Tess4J 5.13.** O Tess4J chama a Leptonica (biblioteca nativa de imagem) e cada
  versão dele espera uma versão exata dela. O Tess4J 5.13 casa com a Leptonica 1.84.1 do Debian 13; versões mais novas
  pedem a 1.85+, que as distros estáveis ainda não empacotam.
- **Rotação medida pelas linhas de texto.** A primeira versão media o ângulo usando todos os pixels escuros, e o ruído
  da foto estragava a medida. Agora o texto é borrado na horizontal até cada linha virar um bloco, blocos pequenos são
  descartados e o ângulo é a mediana dos blocos restantes.
- **CPF mascarado na resposta** (`123.***.***-09`): dado pessoal não trafega inteiro sem necessidade (LGPD).
- **Processamento síncrono.** Cada documento leva menos de 1 segundo, então uma fila seria complexidade sem ganho
  no MVP.

## Próximos passos

Veja [docs/EXERCICIOS.md](docs/EXERCICIOS.md).
