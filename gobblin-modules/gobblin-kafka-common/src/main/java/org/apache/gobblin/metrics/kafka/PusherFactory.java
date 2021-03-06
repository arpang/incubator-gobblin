package org.apache.gobblin.metrics.kafka;

import org.apache.commons.lang3.reflect.ConstructorUtils;

import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import lombok.extern.slf4j.Slf4j;

import org.apache.gobblin.broker.ResourceInstance;
import org.apache.gobblin.broker.StringNameSharedResourceKey;
import org.apache.gobblin.broker.iface.NotConfiguredException;
import org.apache.gobblin.broker.iface.ScopeType;
import org.apache.gobblin.broker.iface.ScopedConfigView;
import org.apache.gobblin.broker.iface.SharedResourceFactory;
import org.apache.gobblin.broker.iface.SharedResourceFactoryResponse;
import org.apache.gobblin.broker.iface.SharedResourcesBroker;


/**
 * Basic resource factory to create shared {@link Pusher} instance
 */
@Slf4j
public abstract class PusherFactory<T, S extends ScopeType<S>> implements SharedResourceFactory<Pusher<T>, StringNameSharedResourceKey, S> {
  private static final String FACTORY_NAME = "pusher";
  private static final String PUSHER_CLASS = "class";

  private static final Config FALLBACK = ConfigFactory.parseMap(
      ImmutableMap.<String, Object>builder()
          .put(PUSHER_CLASS, LoggingPusher.class.getName())
          .build());

  @Override
  public String getName() {
    return FACTORY_NAME;
  }

  @Override
  public SharedResourceFactoryResponse<Pusher<T>> createResource(SharedResourcesBroker<S> broker,
      ScopedConfigView<S, StringNameSharedResourceKey> config)
      throws NotConfiguredException {
    Config pusherConfig = config.getConfig().withFallback(FALLBACK);
    String pusherClass = pusherConfig.getString(PUSHER_CLASS);

    Pusher<T> pusher;
    try {
      pusher = (Pusher) ConstructorUtils.invokeConstructor(Class.forName(pusherClass), pusherConfig);
    } catch (ReflectiveOperationException e) {
      log.warn("Unable to construct a pusher with class {}. LoggingPusher will be used", pusherClass, e);
      pusher = new LoggingPusher<>();
    }
    return new ResourceInstance<>(pusher);
  }
}
