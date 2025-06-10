
# Raft

This project implements the Raft consensus protocol and is stress-tested using [Maelstrom](https://github.com/jepsen-io/maelstrom) under various conditions to ensure correctness and robustness, including basic consensus under normal operation, network latency to test message delays, network partitions to challenge leader election and recovery, and combined fault conditions involving both latency and partitioning.

> [!NOTE]
> To simplify test execution, this project includes VS Code tasks.
> Open the Command Palette (`Ctrl+Shift+P`) and select: `Tasks: Run Task →` choose a pre-defined task such as build or test.

---

## 📦 Dependencies

- **Maelstrom**
- **Java 11** or higher

---

## ⚙️ Build

Make the build script executable and compile the project:

```bash
chmod +x build.sh
./build.sh
```

---

## 🚀 Run Test

Use Maelstrom to test the Raft implementation:

```
./maelstrom.sh test \
  --bin serve.sh \
  -w lin-kv \
  --time-limit 30 \
  --node-count 4 \
  --concurrency 2n \
  --latency 50 \
  --nemesis partition
```

- **node-count**: number of raft servers
- **concurreny**: number of clients per raft server
- **latency**: average message transmission latency
- **nemesis partition**: allow network partitions between raft servers
