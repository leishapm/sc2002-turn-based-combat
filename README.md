# SC2002 Turn-Based Combat Arena

## Team Members

- MENEZES LEISHA PRITIKA  
  leishapr001@e.ntu.edu.sg  

- NADHIM MUZHAFFAR HAKIM  
  nadhim001@e.ntu.edu.sg  

- NATHANAEL NICOLAS CHANG  
  nath0033@e.ntu.edu.sg  

- PENG YIFAN  
  peng0207@e.ntu.edu.sg  

---

## Overview

This project is a command-line turn-based combat game built in Java, designed with strong emphasis on Object-Oriented Design and SOLID principles.

Players select a character, choose items, and battle enemies across multiple difficulty levels using a structured combat system involving:

- Actions  
- Items  
- Status Effects  
- Turn Order Strategies  

The system is designed to be modular, extensible, and maintainable, aligning with the architectural requirements of the assignment :contentReference[oaicite:0]{index=0}.

---

## Game Features

### Characters

Player Classes

- Warrior  
  High HP, balanced stats  
  Special Skill: Shield Bash (damage + stun)

- Wizard  
  High attack, low defense  
  Special Skill: Arcane Blast (AOE + scaling attack buff)

Enemies

- Goblin  
- Wolf  

---

### Actions System

Players can perform:
- Basic Attack  
- Use Item  
- Special Skill (with cooldown)  

Enemies:
- Always perform Basic Attack (designed for extensibility)

---

### Status Effects

- Stun  
  Skips current and next turn  
  Includes proper lifecycle handling:
  - Turn skipped  
  - Turn skipped | Stun expires  

- Smoke Bomb Invulnerability  
  Negates enemy damage for 2 turns  

- Arcane Buff  
  Permanent attack scaling per kill (Wizard)

---

### Items

- Potion  
  Heals HP up to max HP  

- Power Stone  
  Triggers special skill without affecting cooldown  

- Smoke Bomb  
  Grants temporary invulnerability  

Supports duplicates and tracks consumption dynamically.

---

### Levels

| Level | Difficulty | Enemies |
|------|----------|--------|
| 1 | Easy | 3 Goblins |
| 2 | Medium | 1 Goblin + 1 Wolf, Backup: 2 Wolves |
| 3 | Hard | 2 Goblins, Backup: 1 Goblin + 2 Wolves |

Backup waves spawn only after the initial wave is fully defeated.

---

## Game Flow

1. Choose Player  
2. Select 2 Items  
3. Select Level  
4. Battle begins  

Each round:
- Turn order determined by speed  
- Player acts first (implementation choice)  
- Effects are applied and updated  
- Combat continues until:
  - All enemies are defeated (Victory)  
  - Player HP reaches 0 (Defeat)  

---

## Design Highlights

### Architecture

The system is structured into layers:

- Engine Layer for battle management and flow  
- Entities Layer for Player, Enemy, Character  
- Actions Layer for combat logic  
- Effects Layer for status effects  
- UI Layer for CLI interaction  

---

### Key Components

- BattleManagement  
  Controls game loop, rounds, turn order, and win conditions  

- Action and ActionContext  
  Decouples action logic from execution  

- StatusEffect System  
  Handles duration-based effects  

- TurnOrderStrategy  
  Enables flexible turn ordering  

---

### SOLID Principles Applied

- SRP  
  Each class has a single responsibility  

- OCP  
  New actions and effects can be added without modifying core engine  

- LSP  
  Player and Enemy are interchangeable as Character  

- ISP  
  No bloated interfaces  

- DIP  
  Engine depends on abstractions, not concrete classes  

---

## Implementation Details

- HP is clamped between 0 and max HP  

- Damage formula  
  max(0, attack - defense)  

- Status Effects  
  Applied after actions and removed correctly after duration ends  

- Edge cases handled:
  - Stun and death interaction  
  - Item exhaustion  
  - Dynamic turn order updates  
  - Backup spawning  

---

## How to Run

### 1. Clone the Repository

```bash
gh repo clone leishapm/sc2002-turn-based-combat
cd sc2002-turn-based-combat
```

2. Compile and Run
   macOS / Linux
```bash
javac -d out $(find src/main/java -name "*.java")  
java -cp out combatarena.Main
```

  Windows - Command Prompt
```bash
for /R src\main\java %%f in (*.java) do javac -d out "%%f"
java -cp out combatarena.Main
```
  Windows - PowerShell
```bash
javac -d out (Get-ChildItem -Recurse src/main/java -Filter *.java | ForEach-Object { $_.FullName })  
java -cp out combatarena.Main  
```

