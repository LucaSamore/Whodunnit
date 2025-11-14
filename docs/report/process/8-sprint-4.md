# Sprint 4 (27/10/2025 - 02/11/2025)

## Goal

In questo sprint l'obiettivo è implementare meccaniche di gioco fondamentali e definire la base architetturale del livello di controller.
In particolar modo si lavorerà alla progettazione e all'implementazione del sistema di timer, elemento chiave per l'esperienza di gioco, e al meccanismo di trigger per la gestione degli eventi in-game.
Si proseguirà inoltre lo sviluppo dell'interfaccia (con l'integrazione della visualizzazione del timer) e si avvierà la documentazione delle scelte architetturali.
Dal punto di vista dell'architettura, sarà definito e applicato il Cake Pattern per i controller per migliorare modularità e testabilità.

## Sprint Backlog
| Priority |    Product Backlog Item    | Sprint Task                                  |     Assignee      | Initial Estimate of Effort | 1 | 2 | 3 | 4 | 5 | 6 | 7 |
|:--------:|:--------------------------:|:---------------------------------------------|:-----------------:|:--------------------------:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|
|    1     |      FR3 - Versioning      | Logic Implementation (#29)                   | Lucia Castellucci |             5              | 2 | 3 | - | - | - | - | - |
|    2     |        Architecture        | Architecture Design & Implementation (#39)   | Lucia Castellucci |             10             | - | - | 2 | 3 | 2 | 3 | - |
|    2     |           Timer            | Design (#65)                                 |  Roberto Mitugno  |             10             | 3 | - | - | - | - | - | - |
|    2     |           Timer            | Test and Implementation (TDD loop) (#65)     |  Roberto Mitugno  |             10             | - | 2 | 2 | 3 | - | - | - |
|    3     |            View            | Loading Page (#44)                           | Lucia Castellucci |             2              | - | - | - | - | - | - | 2 |
|    3     | FR4 - Trigger/In-game hint | Logic Design and Documentation (#24) - Start |    Luca Samorè    |             12             | - | - | 2 | 2 | - | 1 | 1 |
|    4     |       Documentation        | Development Process (#52)                    |  Roberto Mitugno  |             2              | - | - | - | - | - | - | 2 |

## Review

Il sistema di timer è stato progettato e implementato secondo i principi TDD: include la logica di base per la gestione del tempo e la relativa rappresentazione grafica nell'interfaccia, rendendo visibile l'avanzamento temporale durante le sessioni di gioco.
Il meccanismo di trigger è stato sviluppato e consente ora alla macchina di gioco di reagire a eventi, gestendo transizioni di stato e azioni in-game in modo coerente.
La definizione del Cake Pattern per il livello di controller è stata avviata, fornendo una struttura per la separazione delle responsabilità e per facilitare i test.
È stata inoltre iniziata la documentazione del processo di sviluppo, con note sulle decisioni architetturali e sulle pratiche adottate.

## Retrospective

L'approccio TDD si è dimostrato efficace per preservare qualità e stabilità nelle funzionalità critiche: timer e trigger hanno beneficiato della verifica continua tramite test.
L'investimento in attività architetturali (Cake Pattern) è stato sensato: aver dedicato tempo alla progettazione ha reso più semplice la successiva implementazione e i test dei controller.
Inserire attività di documentazione all'interno dello sprint si è rivelato utile per fissare le decisioni progettuali e ridurre il rischio di perdita di conoscenza.
Per i prossimi cicli sarà importante consolidare l'integrazione tra questi componenti e verificare il comportamento complessivo tramite test di integrazione e sessioni di playtesting mirate.