package org.eclipse.store.examples.reloader;

/*-
 * #%L
 * EclipseStore Example Reloader
 * %%
 * Copyright (C) 2023 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.types.PersistenceManager;
import org.eclipse.serializer.persistence.types.Storer;
import org.eclipse.serializer.persistence.util.Reloader;
import org.eclipse.store.examples.reloader.DataRoot.Item;
import org.eclipse.store.examples.reloader.DataRoot.Test;
import org.eclipse.store.storage.types.StorageManager;

public class ExampleProgram {


  public static void main(final String[] args) {
    final DataRoot root = new DataRoot();
    try (StorageManager storageManager = StorageProvider.createStorageManager("target/data", root)
        .start()) {

      Map<Class<? extends Test>, Map<UUID, Test>> didTypeMap = root.testMap;
      if (!didTypeMap.containsKey(Item.class)) {
        didTypeMap.put(Item.class, new HashMap<>());
        storageManager.store(didTypeMap);
      }
      Map<UUID, Test> map = didTypeMap.get(Item.class);

      var item = new Item("value 3");
      map.put(item.uuid, item);
      Storer eagerStorer = storageManager.createEagerStorer();
      eagerStorer.store(map);
      eagerStorer.commit();

      System.out.printf("Number of items in list: %s%n", root.getItems().size());

      root.testMap.get(Item.class).clear();
      System.out.printf("Number of items in list after clear: %s (surprise :) )%n", root.getItems()
          .size());

      reload(storageManager.persistenceManager(), root);
      System.out.printf("Number of items in list after reload: %s%n", root.getItems()
          .size());

      System.out.println(root.getItems()
          .stream()
          .map(it -> ((Item) it).string)
          .collect(Collectors.joining(", ", "[", "]")));
    }
  }

  private static void reload(final PersistenceManager<Binary> persistenceManager, final DataRoot root) {
    final Reloader reloader = Reloader.New(persistenceManager);
    reloader.reloadDeep(root.testMap);
    // reloadFlat is enough here as we just changed the List object.
    // You can also use .reloadFlat() to reload the object and all objects within the object if you have changed data
    // at multiple levels.
  }
}

