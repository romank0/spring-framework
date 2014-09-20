/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.transaction;

import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

/**
 * @author Juergen Hoeller
 * @since 29.04.2003
 */
@SuppressWarnings("serial")
class TestTransactionManager extends AbstractPlatformTransactionManager {

	private static final Object SAVEPOINT = "savepoint";

	private final Object TRANSACTION = new SimpleTransactionStatus() {

		@Override
		public void rollbackToSavepoint(Object savepoint) throws TransactionException {
			if (savepoint == SAVEPOINT) {
				TestTransactionManager.this.savepointRollback = true;
			}
		}

		@Override
		public void releaseSavepoint(Object savepoint) throws TransactionException {
			if (savepoint == SAVEPOINT) {
				TestTransactionManager.this.savepointRelease = true;
			}
		}

		@Override
		public Object createSavepoint() throws TransactionException {
			TestTransactionManager.this.savepointRelease = false;
			TestTransactionManager.this.savepointRollback = false;
			TestTransactionManager.this.savepoint = true;
			return SAVEPOINT;
		}

		@Override
		public boolean hasSavepoint() {
			return TestTransactionManager.this.savepoint
					&& !TestTransactionManager.this.savepointRelease;
		}

	};

	private final boolean existingTransaction;

	private final boolean canCreateTransaction;

	protected boolean begin = false;

	protected boolean commit = false;

	protected boolean rollback = false;

	protected boolean rollbackOnly = false;

	protected boolean savepoint = false;

	protected boolean savepointRelease = false;

	protected boolean savepointRollback = false;

	protected TestTransactionManager(boolean existingTransaction, boolean canCreateTransaction) {
		this.existingTransaction = existingTransaction;
		this.canCreateTransaction = canCreateTransaction;
		setTransactionSynchronization(SYNCHRONIZATION_NEVER);
	}

	@Override
	protected Object doGetTransaction() {
		return TRANSACTION;
	}

	@Override
	protected boolean isExistingTransaction(Object transaction) {
		return existingTransaction;
	}

	@Override
	protected void doBegin(Object transaction, TransactionDefinition definition) {
		if (!TRANSACTION.equals(transaction)) {
			throw new IllegalArgumentException("Not the same transaction object");
		}
		if (!this.canCreateTransaction) {
			throw new CannotCreateTransactionException("Cannot create transaction");
		}
		this.begin = true;
	}

	@Override
	protected void doCommit(DefaultTransactionStatus status) {
		if (!TRANSACTION.equals(status.getTransaction())) {
			throw new IllegalArgumentException("Not the same transaction object");
		}
		this.commit = true;
	}

	@Override
	protected void doRollback(DefaultTransactionStatus status) {
		if (!TRANSACTION.equals(status.getTransaction())) {
			throw new IllegalArgumentException("Not the same transaction object");
		}
		this.rollback = true;
	}

	@Override
	protected void doSetRollbackOnly(DefaultTransactionStatus status) {
		if (!TRANSACTION.equals(status.getTransaction())) {
			throw new IllegalArgumentException("Not the same transaction object");
		}
		this.rollbackOnly = true;
	}

}
