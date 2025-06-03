# Raft

## Build

```
chmod +x build.sh
./build.sh
```

## Run

```
./maelstrom.sh test --bin serve.sh -w lin-kv --time-limit 30 --node-count 4 --concurrency 2n --latency 50 --nemesis partition
```

- **node-count**: number of raft servers
- **concurreny**: number of clients per raft server
- **latency**: average message transmission latency
- **nemesis partition**: allow network partitions between raft servers

> [!NOTE]  
> To facilitate test execution, some *VScode tasks* have been defined and can be applied using the shortcut `Ctrl+Shift+p`.
