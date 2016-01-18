package com.dianping.squirrel.task;

import com.dianping.cache.entity.ReshardRecord;
import com.dianping.cache.scale.cluster.redis.RedisCluster;
import com.dianping.cache.scale.cluster.redis.RedisManager;
import com.dianping.cache.scale.cluster.redis.RedisServer;
import com.dianping.cache.scale.cluster.redis.ReshardPlan;
import com.dianping.cache.service.ReshardService;
import com.dianping.cache.util.SpringLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by dp on 16/1/6.
 */
public class RedisReshardTask extends AbstractTask {

    private Logger logger = LoggerFactory.getLogger(RedisReshardTask.class);

    private ReshardPlan reshardPlan;

    private ReshardService reshardService;

    public RedisReshardTask(ReshardPlan reshardPlan) {
        this.reshardPlan = reshardPlan;
        reshardService = SpringLocator.getBean("reshardService");
    }

    @Override
    String getTaskType() {
        return "Reshard : " + reshardPlan.getCluster();
    }

    @Override
    int getTaskMinStat() {
        return 0;
    }

    @Override
    int getTaskMaxStat() {
        return reshardPlan.getReshardRecordList().size();
    }

    @Override
    public void taskRun() {
        reshard();
    }

    private void reshard() {
        RedisCluster redisCluster = new RedisCluster(reshardPlan.getSrcNode());
        List<ReshardRecord> reshardRecordList = reshardPlan.getReshardRecordList();
        int stat = 0;
        for (ReshardRecord reshardRecord : reshardRecordList) {
            while (!Thread.currentThread().isInterrupted() ) {
                try {
                    redisCluster.loadClusterInfo();
                    RedisServer src = redisCluster.getServer(reshardRecord.getSrcNode());
                    if (src == null)
                        continue;
                    if (src.getSlotSize() == 0)
                        break;
                    RedisServer des = redisCluster.getServer(reshardRecord.getDesNode());
                    if (reshardRecord.getSlotsDone() < reshardRecord.getSlotsToMigrateCount()) {
                        int slot = src.getSlotList().get(0);
                        boolean result = RedisManager.migrate(src, des, slot);
                        if (result) {
                            reshardRecord.setSlotsDone(reshardRecord.getSlotsDone() + 1);
                            reshardService.updateReshardPlan(reshardPlan);
                        } else {
                            logger.error("Migrate slot : " + slot + " error.");
                            break;
                        }
                    } else {
                        break;
                    }
                } catch (Throwable e) {
                    logger.error("Some Error Occured In Migrating Task",e);
                    reshardPlan.setStatus(400);
                    reshardService.updateReshardPlan(reshardPlan);
                    break;
                }
            }
            updateStat(++stat);
        }
    }


}
