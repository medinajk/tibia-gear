# Tibia Gear

Aplicativo Android para pesquisar equipamentos do jogo **Tibia** e comparar dois itens. Os dados vêm da API pública da [TibiaWiki](https://tibiawiki.dev).

O usuário busca um item, vê os atributos dele (armadura, ataque, defesa, peso…), salva os itens que interessam e compara dois deles lado a lado.

## Como executar

**Requisitos:** Android Studio (versão recente) e um emulador ou celular com Android 8.0 ou superior, conectado à internet.

1. Abra a pasta do projeto no Android Studio (**File → Open**).
2. Aguarde o **Gradle Sync** terminar.
3. Escolha um emulador ou celular e clique em **Run ▶**.

Não é preciso chave de API, senha nem arquivo `.env`.

## Como usar

- **Explorar** (tela inicial): busque um item pelo nome ou navegue pelas categorias.
- **Detalhes**: toque em um item para ver os atributos e salvá-lo.
- **Salvos**: marque dois itens salvos e toque em **Comparar selecionados**.
- **Etapa parcial (Views/XML)**: no menu **⋮** da tela inicial, escolha **Demonstração em Views/XML**. São duas telas em XML com dados simulados, ligadas por Intent explícita.

## Bibliotecas usadas

- **Jetpack Compose + Material 3**: interface das telas principais.
- **Navigation 3**: navegação entre as telas Compose.
- **Lifecycle ViewModel**: guarda o estado de cada tela.
- **Kotlin Coroutines**: chamadas à API sem travar a tela.
- **Retrofit + OkHttp**: acesso à API da TibiaWiki.
- **kotlinx.serialization**: leitura do JSON da API.
- **DataStore**: salva no celular a lista de itens salvos.
- **Coil**: carrega as imagens dos itens.
- **ViewBinding**: liga as telas XML ao código Kotlin.
- **JUnit 4**: testes unitários.
