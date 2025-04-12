package maelstrom.raft.utils;
import maelstrom.raft.state.Log;
import maelstrom.raft.state.LogEntry;
import maelstrom.raft.state.State;


public final class AppendEntries{


    public static void append(final int lPrefixLength, final int lCommitLength, final Log lSuffix, State state){

        int lSuffixLength = lSuffix.size();
        int logLength = state.getLog().size();
        int commitLength = state.getCommitLength();

        if (lSuffixLength > 0 && logLength > lPrefixLength){

            int index = Math.min(logLength, lPrefixLength + lSuffixLength) - 1;
            int entryTerm = state.getLog().get(index).getTerm();
            int lEntryTerm = lSuffix.get(index - lPrefixLength).getTerm();

            if (entryTerm != lEntryTerm){
                // TODO :: DEVIA VERIFICAR O MEU COMMIT INDEX
                state.getLog().truncate(0, lPrefixLength);
                logLength = state.getLog().size();
            }
        }

        if (lPrefixLength + lSuffixLength > logLength){
            for (int index = logLength - lPrefixLength; index < lSuffixLength; index++){
                state.getLog().addLogEntry(lSuffix.get(index));
            }
        }

        if (lCommitLength > commitLength){

            // TODO :: DEVIA VERIFICAR SE NAO ESTOU A DAR READ OVERFLOW NO MEU LOG

            for (int index = commitLength; index < lCommitLength; index++){
                LogEntry logEntry = state.getLog().get(index);
                Deliver.deliver(state, logEntry.getMessage());
            }

            // TODO :: A CONDICAO DO PAPER DO RAFT E MELHOR

            state.setCommitLength(lCommitLength);
        }
    }
}