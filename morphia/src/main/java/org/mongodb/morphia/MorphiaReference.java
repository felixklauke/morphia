/*
 * Copyright (c) 2008-2016 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mongodb.morphia;

import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.QueryException;
import org.mongodb.morphia.query.ValidationException;

public class MorphiaReference<T> {
    private Object id;
    private String type;
    private String collection;

    private transient T entity;
    private transient Class<?> typeClass;

    private MorphiaReference() {
    }

    static <T> MorphiaReference<T> toEntity(final T entity, final Datastore datastore) {
        MorphiaReference<T> ref = new MorphiaReference<T>();
        ref.entity = entity;
        ref.typeClass = entity.getClass();
        ref.type = entity.getClass().getName();
        Object id = datastore.getKey(entity).getId();
        ref.id = id;
        if (id == null) {
            throw new ValidationException("The referenced entity has no ID.  Please save the entity first.");
        }

        return ref;
    }

    static <T> MorphiaReference<T> toEntity(final T entity, final String collection, final Datastore datastore) {
        MorphiaReference<T> ref = toEntity(entity, datastore);
        ref.collection = collection;

        return ref;
    }

    @SuppressWarnings("unchecked")
    T fetch(final Datastore datastore) {
        if (entity == null) {
            try {
                Query<?> query = collection != null
                                 ? ((AdvancedDatastore) datastore).createQuery(collection, getTypeClass())
                                 : datastore.createQuery(getTypeClass());
                entity = (T) query.field("_id").equal(id).get();
            } catch (ClassNotFoundException e) {
                throw new QueryException("No class definition could be found for " + type, e);
            }
        }

        return entity;
    }

    private Class<?> getTypeClass() throws ClassNotFoundException {
        if (typeClass == null) {
            typeClass = Class.forName(type);
        }
        return typeClass;
    }

    /**
     * @return the id of the referenced entity
     */
    public Object getId() {
        return id;
    }

    /**
     * Returns the collection this reference is stored in.  This may differ from the mapped collection name.
     *
     * @return the collection name
     */
    public String getCollection() {
        return collection;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MorphiaReference)) {
            return false;
        }

        final MorphiaReference<?> that = (MorphiaReference<?>) o;

        if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) {
            return false;
        }
        if (type != null ? !type.equals(that.type) : that.type != null) {
            return false;
        }
        return getCollection() != null ? getCollection().equals(that.getCollection()) : that.getCollection() == null;

    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (getCollection() != null ? getCollection().hashCode() : 0);
        return result;
    }
}
