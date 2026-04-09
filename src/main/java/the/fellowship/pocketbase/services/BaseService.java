package the.fellowship.pocketbase.services;

import the.fellowship.pocketbase.PocketBase;

public abstract class BaseService {
    final PocketBase client;

    BaseService(PocketBase client) {
        this.client = client;
    }

    PocketBase getClient() {
        return client;
    }
}
