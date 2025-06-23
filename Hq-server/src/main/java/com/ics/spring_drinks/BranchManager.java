package com.ics.spring_drinks;

import com.ics.models.Branch;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Component
public class BranchManager {
    private final Map<String, Branch> clientBranches = new ConcurrentHashMap<>();
    private final Set<Branch> assignedBranches = new ConcurrentSkipListSet<>();
    private final Object lock = new Object();

    public Branch assignBranch(String clientId, InetAddress clientAddress) throws IOException {
        synchronized (lock) {
            Branch branch = findAvailableBranch(); // The client address is no longer needed for this logic

            if (branch == null) {
                throw new IOException("No available branches. All " + Branch.values().length + " branches are currently occupied.");
            }

            clientBranches.put(clientId, branch);
            assignedBranches.add(branch);

            System.out.println("🏢 Assigned " + branch.name() + " to client " + clientId);
            System.out.println("📊 Branches assigned: " + assignedBranches.size() + "/" + Branch.values().length);

            return branch;
        }
    }

    public void unassignBranch(String clientId, Branch branch) {
        synchronized (lock) {
            // Important: Only unassign if the client and branch match
            if (branch != null && branch.equals(clientBranches.get(clientId))) {
                clientBranches.remove(clientId);
                assignedBranches.remove(branch);
                System.out.println("🔓 Unassigned " + branch.name() + " from client " + clientId);
            }
        }
    }

    /**
     * Finds an available branch based on a "first-come, first-served" policy for the admin branch.
     * The first client to connect gets the NAIROBI branch.
     * Subsequent clients get the next available non-admin branch.
     *
     * @return An available Branch, or null if all are occupied.
     */
    private Branch findAvailableBranch() {
        // Rule 1: If the NAIROBI branch is available, assign it.
        // This ensures the first client connection always gets admin access.
        if (!assignedBranches.contains(Branch.NAIROBI)) {
            return Branch.NAIROBI;
        }

        // Rule 2: If NAIROBI is taken, find any other available branch for regular clients.
        for (Branch branch : Branch.values()) {
            if (branch != Branch.NAIROBI && !assignedBranches.contains(branch)) {
                return branch;
            }
        }

        // Rule 3: If all branches are occupied, return null.
        return null;
    }

    // --- The isLocalConnection method is no longer needed and has been removed ---

    // Getters for monitoring
    public Map<String, Branch> getClientBranches() {
        return new ConcurrentHashMap<>(clientBranches);
    }

    public Set<Branch> getAssignedBranches() {
        return new ConcurrentSkipListSet<>(assignedBranches);
    }
}
//}
//```
//
//        ### Key Changes in This Fix:
//
//        1.  **New `findAvailableBranch()` Logic:** The logic is now much simpler and more direct:
//        * Is `Branch.NAIROBI` free? If yes, assign it. This will apply to the very first client that connects after the server starts.
//        * If `NAIROBI` is taken, loop through the *other* branches and assign the first one that is free.
//2.  **`isLocalConnection()` Removed:** This method is no longer necessary, as the new logic doesn't rely on the client's IP address.
//
//        Now, when you restart your server, the first client you run (which will be your `AdminCli`) is guaranteed to be assigned the `NAIROBI` branch, and you will be able to log in and use the admin functio