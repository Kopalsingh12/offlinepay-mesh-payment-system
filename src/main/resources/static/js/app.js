async function loadWallets() {
    try {
        const response = await fetch("/api/wallets");

        if (!response.ok) {
            throw new Error("Unable to load wallets");
        }

        const wallets = await response.json();

        const totalBalance = wallets.reduce(
            (sum, wallet) => sum + Number(wallet.balance || 0),
            0
        );

        document.getElementById("walletBalance").textContent =
            totalBalance.toFixed(2);

    } catch (error) {
        document.getElementById("walletBalance").textContent =
            "Unavailable";
    }
}

async function loadTransactions() {
    try {
        const response = await fetch("/api/transactions");

        if (!response.ok) {
            throw new Error("Unable to load transactions");
        }

        const transactions = await response.json();

        document.getElementById("transactionCount").textContent =
            transactions.length;

        const table =
            document.getElementById("transactionTable");

        table.innerHTML = "";

        transactions.forEach(transaction => {

            const row = document.createElement("tr");

            row.innerHTML = `
                <td>${transaction.id ?? "-"}</td>
                <td>${transaction.senderWalletId ?? "-"}</td>
                <td>${transaction.receiverWalletId ?? "-"}</td>
                <td>${Number(transaction.amount || 0).toFixed(2)}</td>
                <td>${transaction.status ?? "-"}</td>
            `;

            table.appendChild(row);
        });

    } catch (error) {

        document.getElementById("transactionCount").textContent =
            "Unavailable";

        document.getElementById("transactionTable").innerHTML =
            "<tr><td colspan='5'>Unable to load transactions</td></tr>";
    }
}

async function loadMeshPackets() {
    try {
        const response =
            await fetch("/api/mesh/packets");

        if (!response.ok) {
            throw new Error("Unable to load mesh packets");
        }

        const packets = await response.json();

        document.getElementById("packetCount").textContent =
            packets.length;

    } catch (error) {

        document.getElementById("packetCount").textContent =
            "Unavailable";
    }
}

document
    .getElementById("paymentForm")
    .addEventListener("submit", async function (event) {

        event.preventDefault();

        const senderWalletId =
            document
                .getElementById("senderWalletId")
                .value
                .trim();

        const receiverWalletId =
            document
                .getElementById("receiverWalletId")
                .value
                .trim();

        const amount =
            Number(
                document
                    .getElementById("amount")
                    .value
            );

        const result =
            document.getElementById("paymentResult");

        if (!senderWalletId ||
            !receiverWalletId ||
            !amount ||
            amount <= 0) {

            result.textContent =
                "Please enter valid payment details.";

            return;
        }

        result.textContent =
            "Creating encrypted offline payment...";

        const instruction = {
            senderWalletId: senderWalletId,
            receiverWalletId: receiverWalletId,
            amount: amount,
            nonce: crypto.randomUUID(),
            timestamp: Date.now()
        };

        try {

            const response = await fetch(
                "/api/offline-payments/create" +
                "?senderDeviceId=WEB-DEVICE" +
                "&receiverDeviceId=WEB-RECEIVER",
                {
                    method: "POST",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify(instruction)
                }
            );

            const packet = await response.json();

            if (!response.ok) {
                throw new Error(
                    packet.message ||
                    "Payment creation failed"
                );
            }

            result.innerHTML = `
                <strong>Payment packet created successfully.</strong>
                <br>
                Packet ID: ${packet.packetId}
                <br>
                TTL: ${packet.ttl}
            `;

        } catch (error) {

            result.textContent =
                error.message ||
                "Unable to create payment.";
        }
    });

loadWallets();
loadTransactions();
loadMeshPackets();
