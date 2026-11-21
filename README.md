# Creche Sementinhas: Módulo de Cadastro e Matrícula

Sistema **desktop** desenvolvido como Projeto Integrador do curso de Análise e Desenvolvimento de Sistemas da **FATEC Indaiatuba** (Engenharia de Software II).

O sistema substitui as planilhas de Excel usadas pela Creche Sementinhas no cadastro de crianças, responsáveis e matrículas, com cálculo automático de idade e mensalidade, contrato e ficha de anamnese para impressão e auditoria de alterações.

---

## Tecnologias

| Tecnologia | Versão | Uso |
|---|---|---|
| Java (JDK) | 25 LTS | Linguagem |
| JavaFX | 25.0.5 | Interface gráfica (telas) |
| Maven | 3.9.x | Gerenciamento de dependências e execução |
| PostgreSQL | 18 | Banco de dados |
| Driver JDBC PostgreSQL | 42.7.14 | Conexão Java ↔ banco |

**Arquitetura:** aplicação desktop em rede local (sem internet). Cada computador da creche executa o programa JavaFX, que se conecta ao PostgreSQL instalado em um computador servidor.

---

## 1. Instalação do ambiente (uma vez por computador)

### 1.1 JDK 25
1. Baixe e instale o **JDK 25** (Oracle ou Eclipse Temurin).
2. Teste no terminal:
   ```bash
   java -version
   ```
   A saída deve começar com `java version "25...`.

### 1.2 Maven
1. Baixe o Maven: https://dlcdn.apache.org/maven/maven-3/3.9.16/binaries/apache-maven-3.9.16-bin.zip
2. Crie a pasta `C:\dev` e extraia o zip dentro dela.
3. Confira se existe o arquivo `C:\dev\apache-maven-3.9.16\bin\mvn.cmd`.
4. Adicione a pasta `bin` ao PATH:
   - Tecla **Windows** → digite **variáveis de ambiente** → **Editar as variáveis de ambiente para sua conta**
   - Selecione **Path** → **Editar** → **Novo** → cole `C:\dev\apache-maven-3.9.16\bin` → **OK** → **OK**
5. **Feche e abra o VS Code / terminal** e teste:
   ```bash
   mvn -version
   ```
   Deve mostrar `Apache Maven 3.9.16` e `Java version: 25`.

### 1.3 PostgreSQL e pgAdmin
1. Instale o **PostgreSQL 18** (o pgAdmin já vem junto).
2. Anote a senha do usuário `postgres` definida na instalação.

### 1.4 Ferramentas de desenvolvimento
- **VS Code** com a extensão **Extension Pack for Java**
- **Scene Builder** (Gluon), para desenhar as telas `.fxml`
- **Git**

---

## 2. Baixar o projeto

```bash
git clone https://github.com/Vitao-Campos/creche-sementinhas.git
cd creche-sementinhas
```

> ⚠️ Evite salvar o projeto dentro de pastas sincronizadas pelo **OneDrive**, pois ele pode travar arquivos do Maven e do Git. Prefira algo como `C:\dev\creche-sementinhas`.

---

## 3. Criar o banco de dados

1. Abra o **pgAdmin** e conecte no servidor PostgreSQL.
2. Clique com o botão direito em **Databases** → **Create** → **Database...** → nome: **`creche`** → **Save**.
3. Selecione o banco `creche` → menu **Tools** → **Query Tool**.
4. Abra e execute (botão ▶ ou **F5**), **nesta ordem**:
   1. `database/V1__criar_tabelas.sql` (cria as tabelas)
   2. `database/V2__usuario_admin_inicial.sql` (cria o usuário administrador)
   3. `database/V3__seguranca.sql` (log de acesso e perfil Suporte do admin)

> O login inicial do administrador está no comentário do script `V2__usuario_admin_inicial.sql`.
> ⚠️ Na instalação real na creche, essa senha **deve ser trocada** antes de cadastrar dados reais.
>
> **Regra:** um script que já foi executado **nunca** é alterado. Toda mudança no banco vira um script novo (V4, V5...), e cada integrante executa os que ainda não rodou.
>
> 
## 4. Configurar a conexão com o banco

A senha do banco **nunca** vai para o GitHub. Cada integrante cria o próprio arquivo de configuração:

1. Vá até `src/main/resources/`.
2. Copie o arquivo `db.properties.example` e renomeie a cópia para **`db.properties`**.
3. Preencha com os dados do **seu** PostgreSQL:
   ```properties
   db.url=jdbc:postgresql://localhost:5432/creche
   db.user=postgres
   db.password=SUA_SENHA
   ```

O `db.properties` está no `.gitignore`, então o Git nunca o envia.

---

## 5. Executar o sistema

Na pasta do projeto:

```bash
mvn javafx:run
```

Na primeira execução o Maven baixa as dependências e pode demorar um pouco. Se tudo estiver certo, abre a janela do sistema.

---

## 6. Estrutura do projeto

```
creche-sementinhas/
├── database/                  → scripts SQL do banco
├── pom.xml                    → dependências e configuração do Maven
└── src/main/
    ├── java/br/com/crechesementinhas/
    │   ├── App.java           → ponto de entrada do sistema
    │   ├── model/             → classes de dados (Aluno, Matricula, Responsavel...)
    │   ├── dao/               → acesso ao banco (único lugar com SQL)
    │   ├── service/           → regras de negócio (RN) e requisitos funcionais (RF)
    │   ├── controller/        → lógica das telas FXML
    │   └── util/              → utilitários (ConnectionFactory)
    └── resources/
        ├── db.properties.example → modelo de configuração do banco
        └── db.properties         → SUA configuração (não vai para o Git)
```

**Fluxo entre as camadas:**

```
Tela (FXML) → controller → service → dao → banco
```

Cada camada só conversa com a vizinha: o controller nunca escreve SQL, e o DAO nunca calcula regra de negócio.

---

## 7. Regras de trabalho em grupo (Git)

1. **Sempre antes de começar a trabalhar:**
   ```bash
   git checkout main
   git pull
   ```
2. **Crie uma branch para a sua frente** (nunca trabalhe direto na `main`):
   ```bash
   git checkout -b feature/nome-da-frente
   ```
   Exemplos: `feature/anamnese`, `feature/responsaveis`, `feature/contrato`.
3. **Faça commits pequenos e frequentes**, seguindo o padrão *Conventional Commits*:

   | Prefixo | Quando usar |
   |---|---|
   | `feat:` | Funcionalidade nova |
   | `fix:` | Correção de erro |
   | `refactor:` | Reorganização sem mudar o comportamento |
   | `docs:` | Documentação |
   | `chore:` | Configuração e manutenção |

   Exemplo: `git commit -m "feat: adiciona tela de cadastro de responsaveis"`
4. **Envie a sua branch** e abra um **Pull Request** no GitHub para juntar à `main`:
   ```bash
   git push -u origin feature/nome-da-frente
   ```
5. **Uma tela por pessoa:** arquivos `.fxml` são difíceis de mesclar quando duas pessoas editam o mesmo arquivo.
6. **Alterou o `pom.xml`?** Avise o grupo antes, porque ele é compartilhado por todos.
7. **Nunca** suba o `db.properties`.

---

## 8. Documentação do projeto

Os requisitos seguem a **Especificação de Requisitos, Regras de Negócio e Modelo de Domínio** do grupo:

- **RN-01 a RN-12:** Regras de Negócio
- **RF-01 a RF-11:** Requisitos Funcionais
- **RNF-01 a RNF-03:** Requisitos Não-Funcionais
- **EX-01 a EX-07:** Exceções
- **UC-01:** Cadastrar Aluno e Efetivar Matrícula

Ao implementar uma regra no código, cite o identificador no comentário (ex.: `// RN-07: mensalidade pela faixa etária`). Isso facilita a **Matriz de Rastreabilidade**.
