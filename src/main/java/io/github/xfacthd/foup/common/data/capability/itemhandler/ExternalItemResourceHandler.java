package io.github.xfacthd.foup.common.data.capability.itemhandler;

import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.function.IntPredicate;

public final class ExternalItemResourceHandler extends DelegatingResourceHandler<ItemResource> {
    private final IntPredicate canInsert;
    private final IntPredicate canExtract;

    public ExternalItemResourceHandler(ResourceHandler<ItemResource> delegate, IntPredicate canInsert, IntPredicate canExtract) {
        super(delegate);
        this.canInsert = canInsert;
        this.canExtract = canExtract;
    }

    @Override
    public int insert(int slot, ItemResource resource, int amount, TransactionContext transaction) {
        if (!canInsert.test(slot)) {
            return 0;
        }
        return super.insert(slot, resource, amount, transaction);
    }

    @Override
    public int extract(int slot, ItemResource resource, int amount, TransactionContext ctx) {
        if (!canExtract.test(slot)) {
            return 0;
        }
        return delegate.get().extract(slot, resource, amount, ctx);
    }
}
