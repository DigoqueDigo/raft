package maelstrom.raft.utils;
import maelstrom.raft.state.Log;
import maelstrom.raft.state.LogEntry;
import maelstrom.raft.state.State;


public final class AppendEntries{

    public static void append(final int lPrefixLength, final int lCommitLength, final Log lSuffix, State state){

        int logLength = state.getLog().size();
        final int lSuffixLength = lSuffix.size();
        final int commitLength = state.getCommitLength();

        if (lSuffixLength > 0 && logLength > lPrefixLength){

            int index = Math.min(logLength, lPrefixLength + lSuffixLength) - 1;
            int entryTerm = state.getLog().get(index).getTerm();
            int lEntryTerm = lSuffix.get(index - lPrefixLength).getTerm();

            if (entryTerm != lEntryTerm){
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

            logLength = state.getLog().size();
            final int nextCommitLength = Math.min(lCommitLength, logLength);

            for (int index = commitLength; index < nextCommitLength; index++){
                LogEntry logEntry = state.getLog().get(index);
                Deliver.deliver(state, logEntry.getMessage());
            }

            state.setCommitLength(nextCommitLength);
        }
    }
}